package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiEvaluacionCurso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MiEvaluacionCursoResponse(
        Long usuarioPeriodoEvaluacionId,
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
        LocalDate fechaReal,
        BigDecimal nota,
        Boolean exonerado,
        Boolean esRezagado,
        String observacion,
        String comentarios
) {
    public static MiEvaluacionCursoResponse from(MiEvaluacionCurso m) {
        return new MiEvaluacionCursoResponse(
                m.usuarioPeriodoEvaluacionId(),
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
                m.fechaReal(),
                m.nota(),
                m.exonerado(),
                m.esRezagado(),
                m.observacion(),
                m.comentarios()
        );
    }
}
