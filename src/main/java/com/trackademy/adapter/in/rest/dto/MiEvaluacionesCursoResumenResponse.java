package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiEvaluacionesCursoResumen;

import java.math.BigDecimal;
import java.util.List;

public record MiEvaluacionesCursoResumenResponse(
        BigDecimal promedioAcumulado,
        BigDecimal porcentajeEvaluado,
        long evaluacionesRegistradas,
        long evaluacionesPendientes,
        List<MiEvaluacionCursoResponse> evaluaciones
) {
    public static MiEvaluacionesCursoResumenResponse from(MiEvaluacionesCursoResumen resumen) {
        return new MiEvaluacionesCursoResumenResponse(
                resumen.promedioAcumulado(),
                resumen.porcentajeEvaluado(),
                resumen.evaluacionesRegistradas(),
                resumen.evaluacionesPendientes(),
                resumen.evaluaciones().stream().map(MiEvaluacionCursoResponse::from).toList()
        );
    }
}
