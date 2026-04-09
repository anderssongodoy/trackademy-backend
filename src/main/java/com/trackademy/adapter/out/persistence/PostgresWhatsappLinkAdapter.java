package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.UserWhatsappLinkEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.WhatsappInboundMessageEntity;
import com.trackademy.adapter.out.persistence.entity.WhatsappLinkCodeEntity;
import com.trackademy.adapter.out.persistence.repository.WhatsappInboundMessagePanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UserWhatsappLinkPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.WhatsappLinkCodePanacheRepository;
import com.trackademy.application.port.out.WhatsappLinkPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class PostgresWhatsappLinkAdapter implements WhatsappLinkPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final WhatsappLinkCodePanacheRepository linkCodeRepository;
    private final UserWhatsappLinkPanacheRepository userWhatsappLinkRepository;
    private final WhatsappInboundMessagePanacheRepository inboundMessageRepository;

    public PostgresWhatsappLinkAdapter(
            UsuarioPanacheRepository usuarioRepository,
            WhatsappLinkCodePanacheRepository linkCodeRepository,
            UserWhatsappLinkPanacheRepository userWhatsappLinkRepository,
            WhatsappInboundMessagePanacheRepository inboundMessageRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.linkCodeRepository = linkCodeRepository;
        this.userWhatsappLinkRepository = userWhatsappLinkRepository;
        this.inboundMessageRepository = inboundMessageRepository;
    }

    @Override
    public Optional<WhatsappUserRecord> findUserByEmail(String email) {
        return usuarioRepository.buscarPorEmail(email).map(this::toUserRecord);
    }

    @Override
    public Optional<WhatsappUserRecord> findUserById(Long userId) {
        return Optional.ofNullable(usuarioRepository.findById(userId)).map(this::toUserRecord);
    }

    @Override
    public Optional<WhatsappLinkCodeRecord> findActiveCodeByUserId(Long userId, OffsetDateTime now) {
        return linkCodeRepository.buscarActivoPorUsuario(userId, now).map(this::toCodeRecord);
    }

    @Override
    public Optional<WhatsappLinkCodeRecord> findLatestCodeByCode(String code) {
        return linkCodeRepository.buscarUltimoPorCodigo(code).map(this::toCodeRecord);
    }

    @Override
    @Transactional
    public void cancelActiveCodesForUser(Long userId, OffsetDateTime cancelledAt) {
        linkCodeRepository.cancelarActivosPorUsuario(userId);
    }

    @Override
    @Transactional
    public WhatsappLinkCodeRecord createCode(Long userId, String code, OffsetDateTime expiresAt, OffsetDateTime createdAt) {
        WhatsappLinkCodeEntity entity = new WhatsappLinkCodeEntity();
        entity.userId = userId;
        entity.code = code;
        entity.status = "PENDING";
        entity.expiresAt = expiresAt;
        entity.createdAt = createdAt;
        linkCodeRepository.persist(entity);
        return toCodeRecord(entity);
    }

    @Override
    public Optional<WhatsappUserLinkRecord> findLinkByUserId(Long userId) {
        return userWhatsappLinkRepository.buscarPorUsuarioId(userId).map(this::toLinkRecord);
    }

    @Override
    public Optional<WhatsappUserLinkRecord> findLinkByWaId(String waId) {
        return userWhatsappLinkRepository.buscarPorWaId(waId).map(this::toLinkRecord);
    }

    @Override
    @Transactional
    public WhatsappUserLinkRecord upsertVerifiedLink(Long userId, String waId, String phoneNumber, OffsetDateTime linkedAt) {
        UserWhatsappLinkEntity entity = userWhatsappLinkRepository.buscarPorUsuarioId(userId)
                .orElseGet(() -> {
                    UserWhatsappLinkEntity nuevo = new UserWhatsappLinkEntity();
                    nuevo.userId = userId;
                    return nuevo;
                });

        entity.waId = waId;
        entity.phoneNumber = phoneNumber;
        entity.verified = true;
        entity.linkedAt = linkedAt;
        entity.lastInteractionAt = linkedAt;

        if (entity.id == null) {
            userWhatsappLinkRepository.persist(entity);
        }

        return toLinkRecord(entity);
    }

    @Override
    @Transactional
    public void touchLastInteraction(String waId, OffsetDateTime when) {
        userWhatsappLinkRepository.buscarPorWaId(waId).ifPresent(entity -> entity.lastInteractionAt = when);
    }

    @Override
    @Transactional
    public boolean markInboundMessageIfNew(String metaMessageId, String waId, OffsetDateTime receivedAt) {
        if (metaMessageId == null || metaMessageId.isBlank()) {
            return true;
        }
        if (inboundMessageRepository.buscarPorMetaMessageId(metaMessageId).isPresent()) {
            return false;
        }

        WhatsappInboundMessageEntity entity = new WhatsappInboundMessageEntity();
        entity.metaMessageId = metaMessageId;
        entity.waId = waId;
        entity.receivedAt = receivedAt;
        inboundMessageRepository.persist(entity);
        return true;
    }

    @Override
    @Transactional
    public void markCodeUsed(Long codeId, OffsetDateTime usedAt) {
        WhatsappLinkCodeEntity entity = linkCodeRepository.findById(codeId);
        if (entity == null) {
            return;
        }
        entity.status = "USED";
        entity.usedAt = usedAt;
    }

    @Override
    @Transactional
    public void markCodeExpired(Long codeId) {
        WhatsappLinkCodeEntity entity = linkCodeRepository.findById(codeId);
        if (entity == null || !"PENDING".equals(entity.status)) {
            return;
        }
        entity.status = "EXPIRED";
    }

    @Override
    @Transactional
    public void unlinkByUserId(Long userId) {
        userWhatsappLinkRepository.buscarPorUsuarioId(userId)
                .ifPresent(userWhatsappLinkRepository::delete);
    }

    private WhatsappUserRecord toUserRecord(UsuarioEntity entity) {
        return new WhatsappUserRecord(entity.id, entity.email, entity.nombrePreferido != null ? entity.nombrePreferido : entity.nombre);
    }

    private WhatsappLinkCodeRecord toCodeRecord(WhatsappLinkCodeEntity entity) {
        return new WhatsappLinkCodeRecord(
                entity.id,
                entity.userId,
                entity.code,
                entity.status,
                entity.expiresAt,
                entity.usedAt,
                entity.createdAt
        );
    }

    private WhatsappUserLinkRecord toLinkRecord(UserWhatsappLinkEntity entity) {
        return new WhatsappUserLinkRecord(
                entity.id,
                entity.userId,
                entity.waId,
                entity.phoneNumber,
                Boolean.TRUE.equals(entity.verified),
                entity.linkedAt,
                entity.lastInteractionAt
        );
    }
}
