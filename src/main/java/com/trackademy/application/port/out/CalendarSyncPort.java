package com.trackademy.application.port.out;

import com.trackademy.domain.model.calendar.CalendarSyncPlan;

import java.time.LocalDate;

public interface CalendarSyncPort {

    CalendarSyncPlan obtenerPlanGoogle(String email, LocalDate from, LocalDate to);
}
