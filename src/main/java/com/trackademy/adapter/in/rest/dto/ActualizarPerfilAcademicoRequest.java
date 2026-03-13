package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.ActualizarPerfilAcademicoCommand;

import java.math.BigDecimal;

public record ActualizarPerfilAcademicoRequest(
        BigDecimal metaPromedioCiclo,
        Integer horasEstudioSemanaObjetivo
) {
    public ActualizarPerfilAcademicoCommand toCommand() {
        return new ActualizarPerfilAcademicoCommand(metaPromedioCiclo, horasEstudioSemanaObjetivo);
    }
}
