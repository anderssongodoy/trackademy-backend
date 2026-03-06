package com.trackademy.domain.model;

import java.util.List;

public record CursoDetalle(
        Curso curso,
        String version,
        Integer anio,
        String periodoTexto,
        String sumilla,
        String fundamentacion,
        String metodologia,
        String logroGeneral,
        List<CursoUnidadDetalle> unidades,
        List<CursoEvaluacionDetalle> evaluaciones
) {
}
