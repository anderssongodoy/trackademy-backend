package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.radar.RadarCourseRisk;

import java.math.BigDecimal;
import java.util.List;

public record RadarCourseRiskResponse(
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        BigDecimal accumulatedScore,
        BigDecimal registeredWeight,
        BigDecimal pendingWeight,
        BigDecimal neededAverage,
        String risk,
        Integer score,
        List<String> reasons
) {
    public static RadarCourseRiskResponse from(RadarCourseRisk risk) {
        return new RadarCourseRiskResponse(
                risk.usuarioPeriodoCursoId(),
                risk.cursoId(),
                risk.codigoCurso(),
                risk.nombreCurso(),
                risk.accumulatedScore(),
                risk.registeredWeight(),
                risk.pendingWeight(),
                risk.neededAverage(),
                risk.risk(),
                risk.score(),
                risk.reasons()
        );
    }
}
