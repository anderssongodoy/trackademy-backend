package com.trackademy.domain.model.whatsapp;

import java.time.OffsetDateTime;

public record WhatsappLinkStatus(
        boolean linked,
        String phoneNumberMasked,
        OffsetDateTime linkedAt,
        OffsetDateTime lastInteractionAt
) {
}
