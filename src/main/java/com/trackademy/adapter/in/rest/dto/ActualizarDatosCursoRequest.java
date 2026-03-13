package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.ActualizarDatosCursoCommand;

public record ActualizarDatosCursoRequest(
        String seccion,
        String profesor
) {
    public ActualizarDatosCursoCommand toCommand(Long usuarioPeriodoCursoId) {
        return new ActualizarDatosCursoCommand(usuarioPeriodoCursoId, seccion, profesor);
    }
}
