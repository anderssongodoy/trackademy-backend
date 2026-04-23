package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.calendar.CalendarSyncPlan;
import com.trackademy.domain.model.calendar.CalendarSyncPlanItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CalendarSyncPlanResponse(
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
        List<CalendarSyncPlanItemResponse> items
) {
    public static CalendarSyncPlanResponse from(CalendarSyncPlan plan) {
        return new CalendarSyncPlanResponse(
                plan.provider(),
                plan.connected(),
                plan.accountEmail(),
                plan.calendarId(),
                plan.from(),
                plan.to(),
                plan.creates(),
                plan.updates(),
                plan.deletes(),
                plan.noops(),
                plan.items().stream().map(CalendarSyncPlanItemResponse::from).toList()
        );
    }

    public record CalendarSyncPlanItemResponse(
            String operation,
            String sourceKey,
            String sourceType,
            String title,
            String subtitle,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String courseCode,
            String referenceCode,
            String currentHash,
            String previousHash,
            String googleEventId
    ) {
        public static CalendarSyncPlanItemResponse from(CalendarSyncPlanItem item) {
            return new CalendarSyncPlanItemResponse(
                    item.operation(),
                    item.sourceKey(),
                    item.sourceType(),
                    item.title(),
                    item.subtitle(),
                    item.startAt(),
                    item.endAt(),
                    item.courseCode(),
                    item.referenceCode(),
                    item.currentHash(),
                    item.previousHash(),
                    item.googleEventId()
            );
        }
    }
}
