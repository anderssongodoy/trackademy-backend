package com.trackademy.application.port.out;

import com.trackademy.domain.model.calendar.CalendarSyncPlan;
import com.trackademy.domain.model.calendar.CalendarSyncExecutionResult;
import com.trackademy.domain.model.calendar.CalendarDisconnectResult;

import java.time.LocalDate;

public interface CalendarSyncPort {

    CalendarSyncPlan obtenerPlanGoogle(String email, LocalDate from, LocalDate to);

    CalendarSyncExecutionResult sincronizarGoogle(String email, LocalDate from, LocalDate to);

    CalendarDisconnectResult desconectarGoogle(String email);
}
