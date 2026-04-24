package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "calendar_sync_event")
public class CalendarSyncEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "calendar_sync_account_id", nullable = false)
    public Long calendarSyncAccountId;

    @Column(name = "source_key", nullable = false)
    public String sourceKey;

    @Column(name = "source_type", nullable = false)
    public String sourceType;

    @Column(name = "source_hash", nullable = false)
    public String sourceHash;

    @Column(name = "source_start_at", nullable = false)
    public LocalDateTime sourceStartAt;

    @Column(name = "source_end_at", nullable = false)
    public LocalDateTime sourceEndAt;

    @Column(name = "google_calendar_id")
    public String googleCalendarId;

    @Column(name = "google_event_id")
    public String googleEventId;

    @Column(name = "estado", nullable = false)
    public String estado;

    @Column(name = "error_message")
    public String errorMessage;

    @Column(name = "last_seen_at", nullable = false)
    public OffsetDateTime lastSeenAt;

    @Column(name = "last_synced_at")
    public OffsetDateTime lastSyncedAt;
}
