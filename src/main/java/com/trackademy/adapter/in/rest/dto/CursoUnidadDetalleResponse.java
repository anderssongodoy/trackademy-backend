package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.CursoUnidadDetalle;

import java.util.List;

public record CursoUnidadDetalleResponse(
        Integer nro,
        String titulo,
        Integer semanaInicio,
        Integer semanaFin,
        String logroEspecifico,
        List<String> temas
) {
    public static CursoUnidadDetalleResponse from(CursoUnidadDetalle unidad) {
        return new CursoUnidadDetalleResponse(
                unidad.nro(),
                unidad.titulo(),
                unidad.semanaInicio(),
                unidad.semanaFin(),
                unidad.logroEspecifico(),
                unidad.temas()
        );
    }
}
