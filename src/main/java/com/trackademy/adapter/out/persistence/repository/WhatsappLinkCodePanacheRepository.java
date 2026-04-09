package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.WhatsappLinkCodeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class WhatsappLinkCodePanacheRepository implements PanacheRepositoryBase<WhatsappLinkCodeEntity, Long> {

    public Optional<WhatsappLinkCodeEntity> buscarActivoPorUsuario(Long userId, OffsetDateTime now) {
        return find("userId = ?1 and status = ?2 and expiresAt > ?3 order by id desc", userId, "PENDING", now)
                .firstResultOptional();
    }

    public Optional<WhatsappLinkCodeEntity> buscarUltimoPorCodigo(String code) {
        return find("upper(code) = ?1 order by id desc", code.toUpperCase()).firstResultOptional();
    }

    public long cancelarActivosPorUsuario(Long userId) {
        return update("status = ?1 where userId = ?2 and status = ?3", "CANCELLED", userId, "PENDING");
    }
}
