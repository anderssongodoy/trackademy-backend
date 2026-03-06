package com.trackademy.domain.model.catalogo;

import java.time.LocalDate;

public record PeriodoEventoCatalogo(
        Long id,
        Long periodoId,
        String tipo,
        String titulo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String descripcion
) {
}
