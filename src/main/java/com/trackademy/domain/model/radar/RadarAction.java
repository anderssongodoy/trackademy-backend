package com.trackademy.domain.model.radar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RadarAction(
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
}
