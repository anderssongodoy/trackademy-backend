package com.trackademy.domain.model.me;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MiEvaluacionCurso(
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
}
