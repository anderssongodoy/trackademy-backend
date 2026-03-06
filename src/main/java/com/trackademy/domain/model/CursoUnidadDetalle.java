package com.trackademy.domain.model;

import java.util.List;

public record CursoUnidadDetalle(
        Integer nro,
        String titulo,
        Integer semanaInicio,
        Integer semanaFin,
        String logroEspecifico,
        List<String> temas
) {
}
