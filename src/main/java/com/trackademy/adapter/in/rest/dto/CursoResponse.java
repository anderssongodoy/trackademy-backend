package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.Curso;

public record CursoResponse(
        Long id,
        String codigo,
        String nombre,
        Integer creditos,
        Integer horasSemanales,
        String modalidad
) {
    public static CursoResponse from(Curso curso) {
        return new CursoResponse(
                curso.id(),
                curso.codigo(),
                curso.nombre(),
                curso.creditos(),
                curso.horasSemanales(),
                curso.modalidad()
        );
    }
}
