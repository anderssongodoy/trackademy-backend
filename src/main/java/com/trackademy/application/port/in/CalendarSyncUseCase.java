package com.trackademy.application.port.in;

import com.trackademy.domain.model.calendar.CalendarSyncPlan;

import java.time.LocalDate;

public interface CalendarSyncUseCase {

    CalendarSyncPlan obtenerPlanGoogle(String email, LocalDate from, LocalDate to);
}
