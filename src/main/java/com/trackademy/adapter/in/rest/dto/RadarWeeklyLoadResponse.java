package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.radar.RadarWeeklyLoad;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RadarWeeklyLoadResponse(
        LocalDate from,
        LocalDate to,
        String level,
        Integer pendingEvaluations,
        BigDecimal pendingWeight,
        Integer suggestedStudyMinutes,
        String summary
) {
    public static RadarWeeklyLoadResponse from(RadarWeeklyLoad load) {
        return new RadarWeeklyLoadResponse(
                load.from(),
                load.to(),
                load.level(),
                load.pendingEvaluations(),
                load.pendingWeight(),
                load.suggestedStudyMinutes(),
                load.summary()
        );
    }
}
