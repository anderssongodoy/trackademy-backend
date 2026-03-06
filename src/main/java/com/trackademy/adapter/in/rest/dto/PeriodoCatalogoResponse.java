package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.catalogo.PeriodoCatalogo;

import java.time.LocalDate;

public record PeriodoCatalogoResponse(
        Long id,
        Long universidadId,
        String etiqueta,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado
) {
    public static PeriodoCatalogoResponse from(PeriodoCatalogo x) {
        return new PeriodoCatalogoResponse(
                x.id(),
                x.universidadId(),
                x.etiqueta(),
                x.fechaInicio(),
                x.fechaFin(),
                x.estado()
        );
    }
}
