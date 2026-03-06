package com.trackademy.domain.model;

import java.math.BigDecimal;

public record CursoEvaluacionDetalle(
        String codigo,
        String tipo,
        String descripcion,
        BigDecimal porcentaje,
        Integer semana,
        String observacion
) {
}
