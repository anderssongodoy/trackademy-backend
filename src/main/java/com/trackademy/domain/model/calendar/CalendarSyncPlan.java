package com.trackademy.domain.model.calendar;

import java.time.LocalDate;
import java.util.List;

public record CalendarSyncPlan(
        String provider,
        boolean connected,
        String accountEmail,
        String calendarId,
        LocalDate from,
        LocalDate to,
        long creates,
        long updates,
        long deletes,
        long noops,
        List<CalendarSyncPlanItem> items
) {
}
