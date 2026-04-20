package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.Curso;

import java.util.UUID;

public record CursoResponse(
        Long id,
        UUID publicId,
        String codigo,
        String nombre,
        Integer creditos,
        Integer horasSemanales,
        String modalidad,
        Integer cicloReferencial
) {
    public static CursoResponse from(Curso curso) {
        return new CursoResponse(
                curso.id(),
                curso.publicId(),
                curso.codigo(),
                curso.nombre(),
                curso.creditos(),
                curso.horasSemanales(),
                curso.modalidad(),
                curso.cicloReferencial()
        );
    }
}
