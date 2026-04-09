package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UserWhatsappLinkEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserWhatsappLinkPanacheRepository implements PanacheRepositoryBase<UserWhatsappLinkEntity, Long> {

    public Optional<UserWhatsappLinkEntity> buscarPorUsuarioId(Long userId) {
        return find("userId", userId).firstResultOptional();
    }

    public Optional<UserWhatsappLinkEntity> buscarPorWaId(String waId) {
        return find("waId", waId).firstResultOptional();
    }
}
