package com.trackademy.domain.model.me;

import java.math.BigDecimal;
import java.util.List;

public record MiEvaluacionesCursoResumen(
        BigDecimal promedioAcumulado,
        BigDecimal porcentajeEvaluado,
        long evaluacionesRegistradas,
        long evaluacionesPendientes,
        List<MiEvaluacionCurso> evaluaciones
) {
}
