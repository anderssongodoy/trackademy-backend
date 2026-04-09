package com.trackademy.application.service;

import com.trackademy.application.port.in.WhatsappLinkUseCase;
import com.trackademy.application.port.out.WhatsappLinkPort;
import com.trackademy.domain.model.whatsapp.WhatsappLinkCodeResult;
import com.trackademy.domain.model.whatsapp.WhatsappLinkStatus;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class WhatsappLinkService implements WhatsappLinkUseCase {

    private final WhatsappLinkPort whatsappLinkPort;
    private final String officialNumber;
    private final long ttlSeconds;
    private final SecureRandom random = new SecureRandom();

    public WhatsappLinkService(
            WhatsappLinkPort whatsappLinkPort,
            @ConfigProperty(name = "app.whatsapp.official-number") Optional<String> officialNumber,
            @ConfigProperty(name = "app.whatsapp.link-code.ttl-seconds", defaultValue = "600") long ttlSeconds
    ) {
        this.whatsappLinkPort = whatsappLinkPort;
        this.officialNumber = officialNumber.orElse("");
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public WhatsappLinkCodeResult generateLinkCode(String email) {
        var user = whatsappLinkPort.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        OffsetDateTime now = OffsetDateTime.now();
        whatsappLinkPort.cancelActiveCodesForUser(user.id(), now);

        String code = generateFriendlyCode();
        OffsetDateTime expiresAt = now.plusSeconds(ttlSeconds);
        whatsappLinkPort.createCode(user.id(), code, expiresAt, now);

        String instructions = "Abre WhatsApp y envia este codigo al numero oficial de Trackademy.";
        String prefilledMessage = code;
        String deepLink = buildDeepLink(prefilledMessage);

        return new WhatsappLinkCodeResult(
                code,
                expiresAt,
                officialNumber,
                instructions,
                prefilledMessage,
                deepLink
        );
    }

    @Override
    public WhatsappLinkStatus getLinkStatus(String email) {
        var user = whatsappLinkPort.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        return whatsappLinkPort.findLinkByUserId(user.id())
                .map(link -> new WhatsappLinkStatus(
                        true,
                        maskPhone(link.phoneNumber()),
                        link.linkedAt(),
                        link.lastInteractionAt()
                ))
                .orElse(new WhatsappLinkStatus(false, null, null, null));
    }

    @Override
    public void unlink(String email) {
        var user = whatsappLinkPort.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));
        whatsappLinkPort.unlinkByUserId(user.id());
        whatsappLinkPort.cancelActiveCodesForUser(user.id(), OffsetDateTime.now());
    }

    private String generateFriendlyCode() {
        int value = 100000 + random.nextInt(900000);
        return "TDK-" + value;
    }

    private String buildDeepLink(String message) {
        String normalized = officialNumber == null ? "" : officialNumber.replaceAll("[^0-9]", "");
        if (normalized.isBlank()) {
            return null;
        }
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "https://wa.me/" + normalized + "?text=" + encodedMessage;
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() <= 3) {
            return "***";
        }

        String suffix = digits.substring(Math.max(0, digits.length() - 3));
        String country = digits.length() > 9 ? "+" + digits.substring(0, digits.length() - 9) + " " : "";
        return country + "*** *** " + suffix;
    }
}
