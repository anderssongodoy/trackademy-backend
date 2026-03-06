package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.CursoEvaluacionDetalle;

import java.math.BigDecimal;

public record CursoEvaluacionDetalleResponse(
        String codigo,
        String tipo,
        String descripcion,
        BigDecimal porcentaje,
        Integer semana,
        String observacion
) {
    public static CursoEvaluacionDetalleResponse from(CursoEvaluacionDetalle evaluacion) {
        return new CursoEvaluacionDetalleResponse(
                evaluacion.codigo(),
                evaluacion.tipo(),
                evaluacion.descripcion(),
                evaluacion.porcentaje(),
                evaluacion.semana(),
                evaluacion.observacion()
        );
    }
}
