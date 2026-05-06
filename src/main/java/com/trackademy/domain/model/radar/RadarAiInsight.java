package com.trackademy.domain.model.radar;

import java.util.List;

public record RadarAiInsight(
        String headline,
        String summary,
        String todayAction,
        List<String> weeklyPlan,
        List<String> warnings,
        String confidence,
        String source
) {
}
