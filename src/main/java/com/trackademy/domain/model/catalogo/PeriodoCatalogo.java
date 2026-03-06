package com.trackademy.domain.model.catalogo;

import java.time.LocalDate;

public record PeriodoCatalogo(
        Long id,
        Long universidadId,
        String etiqueta,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado
) {
}
