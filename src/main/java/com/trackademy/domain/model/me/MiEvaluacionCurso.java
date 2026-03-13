package com.trackademy.domain.model.me;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MiEvaluacionCurso(
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
}
