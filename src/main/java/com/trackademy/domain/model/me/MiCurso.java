package com.trackademy.domain.model.me;

public record MiCurso(
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigo,
        String nombre,
        String estado,
        Boolean activo,
        String seccion,
        String profesor,
        String modalidad
) {
}
