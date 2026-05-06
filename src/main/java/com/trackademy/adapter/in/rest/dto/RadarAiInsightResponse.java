package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.radar.RadarAiInsight;

import java.util.List;

public record RadarAiInsightResponse(
        String headline,
        String summary,
        String todayAction,
        List<String> weeklyPlan,
        List<String> warnings,
        String confidence,
        String source
) {
    public static RadarAiInsightResponse from(RadarAiInsight insight) {
        return new RadarAiInsightResponse(
                insight.headline(),
                insight.summary(),
                insight.todayAction(),
                insight.weeklyPlan(),
                insight.warnings(),
                insight.confidence(),
                insight.source()
        );
    }
}
