package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "whatsapp_inbound_message", uniqueConstraints = {
        @UniqueConstraint(name = "uk_whatsapp_inbound_message_meta_id", columnNames = "meta_message_id")
})
public class WhatsappInboundMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "meta_message_id", nullable = false)
    public String metaMessageId;

    @Column(name = "wa_id")
    public String waId;

    @Column(name = "received_at", nullable = false)
    public OffsetDateTime receivedAt;
}
