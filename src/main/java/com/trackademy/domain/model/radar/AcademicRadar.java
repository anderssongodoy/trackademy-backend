package com.trackademy.domain.model.radar;

import java.time.OffsetDateTime;
import java.util.List;

public record AcademicRadar(
        String version,
        OffsetDateTime generatedAt,
        OffsetDateTime validUntil,
        String inputHash,
        boolean aiGenerated,
        String model,
        RadarAiInsight insight,
        RadarAction todayPriority,
        List<RadarAction> topActions,
        RadarWeeklyLoad weeklyLoad,
        List<RadarCourseRisk> courseRisks
) {
}
