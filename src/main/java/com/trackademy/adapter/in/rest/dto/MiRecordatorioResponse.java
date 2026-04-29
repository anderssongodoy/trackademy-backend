package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiRecordatorio;

import java.time.OffsetDateTime;

public record MiRecordatorioResponse(
        Long id,
        Long tareaId,
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        String titulo,
        String descripcion,
        OffsetDateTime fechaEnvio,
        String canal,
        String estado,
        String origen
) {
    public static MiRecordatorioResponse from(MiRecordatorio reminder) {
        return new MiRecordatorioResponse(
                reminder.id(),
                reminder.tareaId(),
                reminder.usuarioPeriodoCursoId(),
                reminder.cursoId(),
                reminder.codigoCurso(),
                reminder.nombreCurso(),
                reminder.titulo(),
                reminder.descripcion(),
                reminder.fechaEnvio(),
                reminder.canal(),
                reminder.estado(),
                reminder.origen()
        );
    }
}
