package com.trackademy.domain.model.me;

import java.time.OffsetDateTime;

public record MiCalendarSyncAccount(
        String provider,
        boolean conectado,
        String email,
        String calendarId,
        String syncDirection,
        String estado,
        OffsetDateTime lastSyncAt
) {
}
