package com.trackademy.domain.model.radar;

import java.math.BigDecimal;
import java.util.List;

public record RadarCourseRisk(
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
}
