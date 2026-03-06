package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.CursoDetalle;

import java.util.List;

public record CursoDetalleResponse(
        CursoResponse curso,
        String version,
        Integer anio,
        String periodoTexto,
        String sumilla,
        String fundamentacion,
        String metodologia,
        String logroGeneral,
        List<CursoUnidadDetalleResponse> unidades,
        List<CursoEvaluacionDetalleResponse> evaluaciones
) {
    public static CursoDetalleResponse from(CursoDetalle detalle) {
        return new CursoDetalleResponse(
                CursoResponse.from(detalle.curso()),
                detalle.version(),
                detalle.anio(),
                detalle.periodoTexto(),
                detalle.sumilla(),
                detalle.fundamentacion(),
                detalle.metodologia(),
                detalle.logroGeneral(),
                detalle.unidades().stream().map(CursoUnidadDetalleResponse::from).toList(),
                detalle.evaluaciones().stream().map(CursoEvaluacionDetalleResponse::from).toList()
        );
    }
}
