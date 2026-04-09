package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_whatsapp_links")
public class UserWhatsappLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "wa_id", nullable = false)
    public String waId;

    @Column(name = "phone_number")
    public String phoneNumber;

    @Column(name = "verified", nullable = false)
    public Boolean verified;

    @Column(name = "linked_at", nullable = false)
    public OffsetDateTime linkedAt;

    @Column(name = "last_interaction_at")
    public OffsetDateTime lastInteractionAt;
}
