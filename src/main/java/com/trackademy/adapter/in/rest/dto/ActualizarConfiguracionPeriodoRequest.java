package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.ActualizarConfiguracionPeriodoCommand;

import java.util.List;

public record ActualizarConfiguracionPeriodoRequest(
        Long campusId,
        Long carreraId,
        Integer cicloActual,
        List<Long> cursoIds
) {
    public ActualizarConfiguracionPeriodoCommand toCommand() {
        return new ActualizarConfiguracionPeriodoCommand(campusId, carreraId, cicloActual, cursoIds);
    }
}
