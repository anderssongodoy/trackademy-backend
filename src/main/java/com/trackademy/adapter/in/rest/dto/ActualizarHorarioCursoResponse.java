package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;

public record ActualizarHorarioCursoResponse(
        Long usuarioPeriodoCursoId,
        int bloquesRegistrados
) {
    public static ActualizarHorarioCursoResponse from(ActualizarHorarioCursoResult result) {
        return new ActualizarHorarioCursoResponse(result.usuarioPeriodoCursoId(), result.bloquesRegistrados());
    }
}
