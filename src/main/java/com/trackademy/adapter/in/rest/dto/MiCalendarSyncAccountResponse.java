package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiCalendarSyncAccount;

import java.time.OffsetDateTime;

public record MiCalendarSyncAccountResponse(
        String provider,
        boolean conectado,
        String email,
        String calendarId,
        String syncDirection,
        String estado,
        OffsetDateTime lastSyncAt
) {
    public static MiCalendarSyncAccountResponse from(MiCalendarSyncAccount account) {
        return new MiCalendarSyncAccountResponse(
                account.provider(),
                account.conectado(),
                account.email(),
                account.calendarId(),
                account.syncDirection(),
                account.estado(),
                account.lastSyncAt()
        );
    }
}
