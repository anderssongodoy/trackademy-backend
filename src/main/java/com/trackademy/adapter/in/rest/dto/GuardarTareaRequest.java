package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.GuardarTareaCommand;

import java.time.OffsetDateTime;

public record GuardarTareaRequest(
        Long usuarioPeriodoCursoId,
        String titulo,
        String descripcion,
        String tipo,
        String prioridad,
        String estado,
        OffsetDateTime fechaVencimiento,
        OffsetDateTime fechaRecordatorio,
        String canalRecordatorio
) {
    public GuardarTareaCommand toCommand() {
        return new GuardarTareaCommand(
                usuarioPeriodoCursoId,
                titulo,
                descripcion,
                tipo,
                prioridad,
                estado,
                fechaVencimiento,
                fechaRecordatorio,
                canalRecordatorio
        );
    }
}
