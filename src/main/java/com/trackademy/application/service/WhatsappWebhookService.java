package com.trackademy.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.trackademy.application.port.in.WhatsappWebhookUseCase;
import com.trackademy.application.port.out.WhatsappLinkPort;
import com.trackademy.application.port.out.WhatsappMessagePort;
import com.trackademy.domain.model.whatsapp.WspResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class WhatsappWebhookService implements WhatsappWebhookUseCase {

    private static final Pattern LINK_CODE_PATTERN = Pattern.compile("^TDK-\\d{6}$");

    private final WhatsappLinkPort whatsappLinkPort;
    private final WhatsappMessagePort whatsappMessagePort;
    private final WhatsappCommandService whatsappCommandService;
    private final String verifyToken;
    private final String appSecret;

    public WhatsappWebhookService(
            WhatsappLinkPort whatsappLinkPort,
            WhatsappMessagePort whatsappMessagePort,
            WhatsappCommandService whatsappCommandService,
            @ConfigProperty(name = "app.whatsapp.meta.webhook-verify-token") Optional<String> verifyToken,
            @ConfigProperty(name = "app.whatsapp.meta.app-secret") Optional<String> appSecret
    ) {
        this.whatsappLinkPort = whatsappLinkPort;
        this.whatsappMessagePort = whatsappMessagePort;
        this.whatsappCommandService = whatsappCommandService;
        this.verifyToken = verifyToken.orElse("");
        this.appSecret = appSecret.orElse("");
    }

    @Override
    public Optional<String> verifyWebhook(String mode, String providedVerifyToken, String challenge) {
        if (!"subscribe".equals(mode)) return Optional.empty();
        if (verifyToken == null || verifyToken.isBlank() || !verifyToken.equals(providedVerifyToken)) return Optional.empty();
        return Optional.ofNullable(challenge);
    }

    @Override
    public boolean isSignatureValid(byte[] rawPayload, String signatureHeader) {
        if (appSecret == null || appSecret.isBlank()) return true;
        if (rawPayload == null || signatureHeader == null || signatureHeader.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawPayload);
            String expected = "sha256=" + toHex(digest);
            return expected.equalsIgnoreCase(signatureHeader.trim());
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void handleIncomingWebhook(JsonNode payload) {
        if (payload == null || !payload.has("entry")) return;
        for (JsonNode entry : payload.path("entry")) {
            for (JsonNode change : entry.path("changes")) {
                JsonNode messages = change.path("value").path("messages");
                if (!messages.isArray()) continue;
                for (JsonNode message : messages) {
                    processIncomingMessage(message);
                }
            }
        }
    }

    private void processIncomingMessage(JsonNode message) {
        String type = message.path("type").asText();
        String metaMessageId = trimToNull(message.path("id").asText());
        String waId = trimToNull(message.path("from").asText());
        if (waId == null) return;

        String text = null;
        if ("text".equals(type)) {
            text = trimToNull(message.path("text").path("body").asText());
        } else if ("interactive".equals(type)) {
            String interactiveType = message.path("interactive").path("type").asText();
            if ("button_reply".equals(interactiveType)) {
                text = trimToNull(message.path("interactive").path("button_reply").path("id").asText());
            } else if ("list_reply".equals(interactiveType)) {
                text = trimToNull(message.path("interactive").path("list_reply").path("id").asText());
            } else {
                return;
            }
        } else {
            return;
        }
        if (text == null) return;

        OffsetDateTime now = OffsetDateTime.now();
        if (!whatsappLinkPort.markInboundMessageIfNew(metaMessageId, waId, now)) return;

        Optional<WhatsappLinkPort.WhatsappUserLinkRecord> linked = whatsappLinkPort.findLinkByWaId(waId);
        if (linked.isPresent()) {
            whatsappLinkPort.touchLastInteraction(waId, now);
            var user = whatsappLinkPort.findUserById(linked.get().userId()).orElse(null);
            if (user == null) {
                whatsappMessagePort.send(waId, new WspResponse.Texto(
                        "No pude identificar tu cuenta de Trackademy. Vuelve a vincular tu numero desde la web."));
                return;
            }
            WspResponse reply = whatsappCommandService.resolveCommand(user.email(), text);
            whatsappMessagePort.send(waId, reply);
            return;
        }

        if ("text".equals(type)) {
            String normalizedCode = text.trim().toUpperCase(Locale.ROOT);
            if (LINK_CODE_PATTERN.matcher(normalizedCode).matches()) {
                handleLinkCode(waId, normalizedCode, now);
                return;
            }
        }

        whatsappMessagePort.send(waId, new WspResponse.Texto(
                "Tu numero aun no esta vinculado a una cuenta de Trackademy. Ingresa a la web, genera tu codigo de vinculacion y envialo por este chat."));
    }

    private void handleLinkCode(String waId, String code, OffsetDateTime now) {
        Optional<WhatsappLinkPort.WhatsappLinkCodeRecord> codeOpt = whatsappLinkPort.findLatestCodeByCode(code);
        if (codeOpt.isEmpty()) {
            whatsappMessagePort.send(waId, new WspResponse.Texto(
                    "No reconoci ese codigo. Genera uno nuevo desde tu cuenta de Trackademy e intentalo otra vez."));
            return;
        }

        WhatsappLinkPort.WhatsappLinkCodeRecord linkCode = codeOpt.get();
        if (!"PENDING".equals(linkCode.status())) {
            whatsappMessagePort.send(waId, new WspResponse.Texto(
                    "No reconoci ese codigo. Genera uno nuevo desde tu cuenta de Trackademy e intentalo otra vez."));
            return;
        }

        if (linkCode.expiresAt() == null || !linkCode.expiresAt().isAfter(now)) {
            whatsappLinkPort.markCodeExpired(linkCode.id());
            whatsappMessagePort.send(waId, new WspResponse.Texto(
                    "Este codigo de vinculacion ya expiro. Genera uno nuevo desde tu cuenta de Trackademy."));
            return;
        }

        Optional<WhatsappLinkPort.WhatsappUserLinkRecord> existingByWaId = whatsappLinkPort.findLinkByWaId(waId);
        if (existingByWaId.isPresent() && !existingByWaId.get().userId().equals(linkCode.userId())) {
            whatsappMessagePort.send(waId, new WspResponse.Texto(
                    "Este numero ya esta vinculado a otra cuenta de Trackademy. Revisa tu configuracion desde la web."));
            return;
        }

        whatsappLinkPort.upsertVerifiedLink(linkCode.userId(), waId, waId, now);
        whatsappLinkPort.markCodeUsed(linkCode.id(), now);
        whatsappMessagePort.send(waId, new WspResponse.Botones(
                "Tu cuenta de Trackademy fue vinculada correctamente. Que quieres consultar?",
                List.of(
                        new WspResponse.Botones.Boton("hoy", "Hoy"),
                        new WspResponse.Botones.Boton("semana", "Esta semana"),
                        new WspResponse.Botones.Boton("menu", "Menu")
                )
        ));
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toHex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) out.append(String.format("%02x", value));
        return out.toString();
    }
}
