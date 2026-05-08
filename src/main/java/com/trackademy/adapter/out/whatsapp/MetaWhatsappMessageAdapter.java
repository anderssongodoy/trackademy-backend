package com.trackademy.adapter.out.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.application.port.out.WhatsappMessagePort;
import com.trackademy.domain.model.whatsapp.WspResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MetaWhatsappMessageAdapter implements WhatsappMessagePort {

    private static final Logger LOG = Logger.getLogger(MetaWhatsappMessageAdapter.class);

    private final ObjectMapper objectMapper;
    private final String accessToken;
    private final String phoneNumberId;
    private final String apiVersion;
    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, Instant> lastSendByRecipient = new ConcurrentHashMap<>();

    public MetaWhatsappMessageAdapter(
            ObjectMapper objectMapper,
            @ConfigProperty(name = "app.whatsapp.meta.access-token") Optional<String> accessToken,
            @ConfigProperty(name = "app.whatsapp.meta.phone-number-id") Optional<String> phoneNumberId,
            @ConfigProperty(name = "app.whatsapp.meta.api-version", defaultValue = "v23.0") String apiVersion
    ) {
        this.objectMapper = objectMapper;
        this.accessToken = accessToken.orElse("");
        this.phoneNumberId = phoneNumberId.orElse("");
        this.apiVersion = apiVersion;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void send(String to, WspResponse response) {
        if (to == null || to.isBlank() || response == null) return;
        if (accessToken == null || accessToken.isBlank() || phoneNumberId == null || phoneNumberId.isBlank()) {
            LOG.warn("WhatsApp message skipped because Meta config is incomplete.");
            return;
        }
        if (!canSendNow(to)) {
            LOG.warnf("WhatsApp message throttled for recipient %s", to);
            return;
        }

        try {
            String payload = switch (response) {
                case WspResponse.Texto t -> buildTextPayload(to, t.body());
                case WspResponse.Botones b -> buildBotonPayload(to, b);
                case WspResponse.Lista l -> buildListaPayload(to, l);
            };
            doSend(to, payload);
        } catch (Exception ex) {
            lastSendByRecipient.remove(to);
            LOG.error("WhatsApp send failed due to integration error.", ex);
        }
    }

    private String buildTextPayload(String to, String body) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "text",
                "text", Map.of("preview_url", false, "body", body)
        ));
    }

    private String buildBotonPayload(String to, WspResponse.Botones botones) throws Exception {
        var buttons = botones.botones().stream()
                .map(b -> Map.of("type", "reply", "reply", Map.of("id", b.id(), "title", b.etiqueta())))
                .toList();
        return objectMapper.writeValueAsString(Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "button",
                        "body", Map.of("text", botones.body()),
                        "action", Map.of("buttons", buttons)
                )
        ));
    }

    private String buildListaPayload(String to, WspResponse.Lista lista) throws Exception {
        var sections = lista.secciones().stream()
                .map(s -> {
                    var rows = s.items().stream()
                            .map(item -> Map.of("id", item.id(), "title", item.titulo(), "description", item.descripcion()))
                            .toList();
                    return Map.of("title", s.titulo(), "rows", rows);
                })
                .toList();
        return objectMapper.writeValueAsString(Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "list",
                        "body", Map.of("text", lista.body()),
                        "action", Map.of("button", lista.botonAbrir(), "sections", sections)
                )
        ));
    }

    private void doSend(String to, String payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            LOG.errorf("WhatsApp send failed status=%d body=%s", response.statusCode(), response.body());
            lastSendByRecipient.remove(to);
        }
    }

    private boolean canSendNow(String to) {
        Instant now = Instant.now();
        Instant previous = lastSendByRecipient.putIfAbsent(to, now);
        if (previous == null) return true;
        if (Duration.between(previous, now).getSeconds() < 6) return false;
        lastSendByRecipient.put(to, now);
        return true;
    }
}
