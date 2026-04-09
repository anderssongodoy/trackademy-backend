package com.trackademy.application.port.out;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface WhatsappLinkPort {

    Optional<WhatsappUserRecord> findUserByEmail(String email);

    Optional<WhatsappUserRecord> findUserById(Long userId);

    Optional<WhatsappLinkCodeRecord> findActiveCodeByUserId(Long userId, OffsetDateTime now);

    Optional<WhatsappLinkCodeRecord> findLatestCodeByCode(String code);

    void cancelActiveCodesForUser(Long userId, OffsetDateTime cancelledAt);

    WhatsappLinkCodeRecord createCode(Long userId, String code, OffsetDateTime expiresAt, OffsetDateTime createdAt);

    Optional<WhatsappUserLinkRecord> findLinkByUserId(Long userId);

    Optional<WhatsappUserLinkRecord> findLinkByWaId(String waId);

    WhatsappUserLinkRecord upsertVerifiedLink(Long userId, String waId, String phoneNumber, OffsetDateTime linkedAt);

    void touchLastInteraction(String waId, OffsetDateTime when);

    boolean markInboundMessageIfNew(String metaMessageId, String waId, OffsetDateTime receivedAt);

    void markCodeUsed(Long codeId, OffsetDateTime usedAt);

    void markCodeExpired(Long codeId);

    void unlinkByUserId(Long userId);

    record WhatsappUserRecord(
            Long id,
            String email,
            String name
    ) {
    }

    record WhatsappLinkCodeRecord(
            Long id,
            Long userId,
            String code,
            String status,
            OffsetDateTime expiresAt,
            OffsetDateTime usedAt,
            OffsetDateTime createdAt
    ) {
    }

    record WhatsappUserLinkRecord(
            Long id,
            Long userId,
            String waId,
            String phoneNumber,
            boolean verified,
            OffsetDateTime linkedAt,
            OffsetDateTime lastInteractionAt
    ) {
    }
}
