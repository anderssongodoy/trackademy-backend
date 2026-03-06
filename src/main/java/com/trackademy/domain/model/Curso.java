package com.trackademy.domain.model;

public record Curso(
        Long id,
        String codigo,
        String nombre,
        Integer creditos,
        Integer horasSemanales,
        String modalidad
) {
}
