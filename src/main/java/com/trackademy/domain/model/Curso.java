package com.trackademy.domain.model;

import java.util.UUID;

public record Curso(
        Long id,
        UUID publicId,
        String codigo,
        String nombre,
        Integer creditos,
        Integer horasSemanales,
        String modalidad,
        Integer cicloReferencial
) {
}
