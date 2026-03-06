package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiCurso;

public record MiCursoResponse(
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
    public static MiCursoResponse from(MiCurso c) {
        return new MiCursoResponse(
                c.usuarioPeriodoCursoId(),
                c.cursoId(),
                c.codigo(),
                c.nombre(),
                c.estado(),
                c.activo(),
                c.seccion(),
                c.profesor(),
                c.modalidad()
        );
    }
}
