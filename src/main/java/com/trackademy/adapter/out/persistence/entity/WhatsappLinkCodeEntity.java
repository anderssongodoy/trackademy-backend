package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "whatsapp_link_codes")
public class WhatsappLinkCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "code", nullable = false)
    public String code;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "expires_at", nullable = false)
    public OffsetDateTime expiresAt;

    @Column(name = "used_at")
    public OffsetDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;
}
