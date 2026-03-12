package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiEvaluacionCurso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MiEvaluacionCursoResponse(
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        String evaluacionCodigo,
        String tipo,
        String descripcion,
        BigDecimal porcentaje,
        Integer semana,
        LocalDate fechaEstimada,
        String observacion
) {
    public static MiEvaluacionCursoResponse from(MiEvaluacionCurso m) {
        return new MiEvaluacionCursoResponse(
                m.usuarioPeriodoCursoId(),
                m.cursoId(),
                m.codigoCurso(),
                m.nombreCurso(),
                m.evaluacionCodigo(),
                m.tipo(),
                m.descripcion(),
                m.porcentaje(),
                m.semana(),
                m.fechaEstimada(),
                m.observacion()
        );
    }
}
