package com.trackademy.domain.model.calendar;

import java.time.LocalDateTime;

public record CalendarSyncPlanItem(
        String operation,
        String sourceKey,
        String sourceType,
        String title,
        String subtitle,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer reminderMinutesBefore,
        String courseCode,
        String referenceCode,
        String currentHash,
        String previousHash,
        String googleEventId
) {
}
