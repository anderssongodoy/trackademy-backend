package com.trackademy.domain.model.radar;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RadarWeeklyLoad(
        LocalDate from,
        LocalDate to,
        String level,
        Integer pendingEvaluations,
        BigDecimal pendingWeight,
        Integer suggestedStudyMinutes,
        String summary
) {
}
