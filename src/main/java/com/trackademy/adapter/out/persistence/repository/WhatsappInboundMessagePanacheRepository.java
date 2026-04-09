package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.WhatsappInboundMessageEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class WhatsappInboundMessagePanacheRepository implements PanacheRepositoryBase<WhatsappInboundMessageEntity, Long> {

    public Optional<WhatsappInboundMessageEntity> buscarPorMetaMessageId(String metaMessageId) {
        return find("metaMessageId", metaMessageId).firstResultOptional();
    }
}
