package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.radar.AcademicRadar;

import java.time.OffsetDateTime;
import java.util.List;

public record AcademicRadarResponse(
        String version,
        OffsetDateTime generatedAt,
        OffsetDateTime validUntil,
        String inputHash,
        boolean aiGenerated,
        String model,
        RadarAiInsightResponse insight,
        RadarActionResponse todayPriority,
        List<RadarActionResponse> topActions,
        RadarWeeklyLoadResponse weeklyLoad,
        List<RadarCourseRiskResponse> courseRisks
) {
    public static AcademicRadarResponse from(AcademicRadar radar) {
        return new AcademicRadarResponse(
                radar.version(),
                radar.generatedAt(),
                radar.validUntil(),
                radar.inputHash(),
                radar.aiGenerated(),
                radar.model(),
                RadarAiInsightResponse.from(radar.insight()),
                radar.todayPriority() == null ? null : RadarActionResponse.from(radar.todayPriority()),
                radar.topActions().stream().map(RadarActionResponse::from).toList(),
                RadarWeeklyLoadResponse.from(radar.weeklyLoad()),
                radar.courseRisks().stream().map(RadarCourseRiskResponse::from).toList()
        );
    }
}
