package com.trackademy.domain.model.calendar;

public record CalendarDisconnectResult(
        String provider,
        boolean disconnected,
        long removedMappings
) {
}
