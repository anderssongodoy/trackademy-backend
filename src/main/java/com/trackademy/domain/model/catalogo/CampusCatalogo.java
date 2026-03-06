package com.trackademy.domain.model.catalogo;

public record CampusCatalogo(
        Long id,
        Long universidadId,
        String nombre,
        String timezone
) {
}
