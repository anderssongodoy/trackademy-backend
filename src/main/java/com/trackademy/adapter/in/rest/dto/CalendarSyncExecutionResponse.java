package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.calendar.CalendarDisconnectResult;
import com.trackademy.domain.model.calendar.CalendarSyncExecutionResult;

import java.time.LocalDate;

public class CalendarSyncExecutionResponse {

    public record SyncResponse(
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
        public static SyncResponse from(CalendarSyncExecutionResult result) {
            return new SyncResponse(
                    result.provider(),
                    result.connected(),
                    result.accountEmail(),
                    result.calendarId(),
                    result.from(),
                    result.to(),
                    result.created(),
                    result.updated(),
                    result.deleted(),
                    result.unchanged(),
                    result.failed()
            );
        }
    }

    public record DisconnectResponse(
            String provider,
            boolean disconnected,
            long removedMappings
    ) {
        public static DisconnectResponse from(CalendarDisconnectResult result) {
            return new DisconnectResponse(
                    result.provider(),
                    result.disconnected(),
                    result.removedMappings()
            );
        }
    }
}
