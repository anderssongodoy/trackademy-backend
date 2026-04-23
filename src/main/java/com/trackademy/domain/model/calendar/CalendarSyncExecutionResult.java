package com.trackademy.domain.model.calendar;

import java.time.LocalDate;

public record CalendarSyncExecutionResult(
        String provider,
        boolean connected,
        String accountEmail,
        String calendarId,
        LocalDate from,
        LocalDate to,
        long created,
        long updated,
        long deleted,
        long unchanged,
        long failed
) {
}
