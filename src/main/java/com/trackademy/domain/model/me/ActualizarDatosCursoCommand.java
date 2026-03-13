package com.trackademy.domain.model.me;

public record ActualizarDatosCursoCommand(
        Long usuarioPeriodoCursoId,
        String seccion,
        String profesor
) {
}
