package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.whatsapp.WhatsappLinkStatus;

import java.time.OffsetDateTime;

public record WhatsappLinkStatusResponse(
        boolean linked,
        String phoneNumberMasked,
        OffsetDateTime linkedAt,
        OffsetDateTime lastInteractionAt
) {

    public static WhatsappLinkStatusResponse from(WhatsappLinkStatus status) {
        return new WhatsappLinkStatusResponse(
                status.linked(),
                status.phoneNumberMasked(),
                status.linkedAt(),
                status.lastInteractionAt()
        );
    }
}
