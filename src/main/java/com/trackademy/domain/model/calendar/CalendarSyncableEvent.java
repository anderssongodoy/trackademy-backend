package com.trackademy.domain.model.calendar;

import java.time.LocalDateTime;

public record CalendarSyncableEvent(
        String sourceKey,
        String sourceType,
        String sourceHash,
        String title,
        String subtitle,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean allDay,
        String origin,
        String courseCode,
        String courseName,
        String referenceCode
) {
}
