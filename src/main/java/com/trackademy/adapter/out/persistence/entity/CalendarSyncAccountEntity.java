package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "calendar_sync_account")
public class CalendarSyncAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_id", nullable = false)
    public Long usuarioId;

    @Column(name = "provider", nullable = false)
    public String provider;

    @Column(name = "external_account_id")
    public String externalAccountId;

    @Column(name = "email")
    public String email;

    @Column(name = "calendar_id")
    public String calendarId;

    @Column(name = "sync_direction", nullable = false)
    public String syncDirection = "bidirectional";

    @Column(name = "access_token_encrypted")
    public String accessTokenEncrypted;

    @Column(name = "refresh_token_encrypted")
    public String refreshTokenEncrypted;

    @Column(name = "token_expires_at")
    public OffsetDateTime tokenExpiresAt;

    @Column(name = "estado", nullable = false)
    public String estado = "active";

    @Column(name = "last_sync_at")
    public OffsetDateTime lastSyncAt;
}
