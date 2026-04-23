package com.trackademy.application.service;

import com.trackademy.application.port.in.CalendarSyncUseCase;
import com.trackademy.application.port.out.CalendarSyncPort;
import com.trackademy.domain.model.calendar.CalendarDisconnectResult;
import com.trackademy.domain.model.calendar.CalendarSyncExecutionResult;
import com.trackademy.domain.model.calendar.CalendarSyncPlan;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

@ApplicationScoped
public class CalendarSyncService implements CalendarSyncUseCase {

    private final CalendarSyncPort calendarSyncPort;

    public CalendarSyncService(CalendarSyncPort calendarSyncPort) {
        this.calendarSyncPort = calendarSyncPort;
    }

    @Override
    public CalendarSyncPlan obtenerPlanGoogle(String email, LocalDate from, LocalDate to) {
        return calendarSyncPort.obtenerPlanGoogle(email, from, to);
    }

    @Override
    public CalendarSyncExecutionResult sincronizarGoogle(String email, LocalDate from, LocalDate to) {
        return calendarSyncPort.sincronizarGoogle(email, from, to);
    }

    @Override
    public CalendarDisconnectResult desconectarGoogle(String email) {
        return calendarSyncPort.desconectarGoogle(email);
    }
}
