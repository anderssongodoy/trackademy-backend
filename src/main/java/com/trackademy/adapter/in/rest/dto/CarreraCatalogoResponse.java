package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.catalogo.CarreraCatalogo;

public record CarreraCatalogoResponse(
        Long id,
        Long universidadId,
        String nombre
) {
    public static CarreraCatalogoResponse from(CarreraCatalogo x) {
        return new CarreraCatalogoResponse(x.id(), x.universidadId(), x.nombre());
    }
}
