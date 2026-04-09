package com.trackademy.domain.model.whatsapp;

import java.time.OffsetDateTime;

public record WhatsappLinkCodeResult(
        String code,
        OffsetDateTime expiresAt,
        String officialWhatsappNumber,
        String instructions,
        String prefilledMessage,
        String deepLink
) {
}
