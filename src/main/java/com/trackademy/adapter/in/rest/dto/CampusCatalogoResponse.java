package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.catalogo.CampusCatalogo;

public record CampusCatalogoResponse(
        Long id,
        Long universidadId,
        String nombre,
        String timezone
) {
    public static CampusCatalogoResponse from(CampusCatalogo x) {
        return new CampusCatalogoResponse(x.id(), x.universidadId(), x.nombre(), x.timezone());
    }
}
