package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.radar.RadarAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RadarActionResponse(
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        String evaluacionCodigo,
        String tipo,
        LocalDate fechaEstimada,
        BigDecimal porcentaje,
        Integer suggestedMinutes,
        Integer score,
        String priority,
        List<String> reasons
) {
    public static RadarActionResponse from(RadarAction action) {
        return new RadarActionResponse(
                action.usuarioPeriodoCursoId(),
                action.cursoId(),
                action.codigoCurso(),
                action.nombreCurso(),
                action.evaluacionCodigo(),
                action.tipo(),
                action.fechaEstimada(),
                action.porcentaje(),
                action.suggestedMinutes(),
                action.score(),
                action.priority(),
                action.reasons()
        );
    }
}
