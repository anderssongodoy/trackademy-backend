package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.whatsapp.WhatsappLinkCodeResult;

import java.time.OffsetDateTime;

public record WhatsappLinkCodeResponse(
        String code,
        OffsetDateTime expiresAt,
        String officialWhatsappNumber,
        String instructions,
        String prefilledMessage,
        String deepLink
) {

    public static WhatsappLinkCodeResponse from(WhatsappLinkCodeResult result) {
        return new WhatsappLinkCodeResponse(
                result.code(),
                result.expiresAt(),
                result.officialWhatsappNumber(),
                result.instructions(),
                result.prefilledMessage(),
                result.deepLink()
        );
    }
}
