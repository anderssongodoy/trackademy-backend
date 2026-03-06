package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.catalogo.PeriodoEventoCatalogo;

import java.time.LocalDate;

public record PeriodoEventoCatalogoResponse(
        Long id,
        Long periodoId,
        String tipo,
        String titulo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String descripcion
) {
    public static PeriodoEventoCatalogoResponse from(PeriodoEventoCatalogo x) {
        return new PeriodoEventoCatalogoResponse(
                x.id(),
                x.periodoId(),
                x.tipo(),
                x.titulo(),
                x.fechaInicio(),
                x.fechaFin(),
                x.descripcion()
        );
    }
}
