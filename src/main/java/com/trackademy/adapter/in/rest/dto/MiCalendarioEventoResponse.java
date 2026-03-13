package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiCalendarioEvento;

import java.time.LocalDateTime;

public record MiCalendarioEventoResponse(
        String origen,
        String tipo,
        String titulo,
        String subtitulo,
        LocalDateTime inicio,
        LocalDateTime fin,
        boolean todoElDia,
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        String referenciaCodigo
) {
    public static MiCalendarioEventoResponse from(MiCalendarioEvento evento) {
        return new MiCalendarioEventoResponse(
                evento.origen(),
                evento.tipo(),
                evento.titulo(),
                evento.subtitulo(),
                evento.inicio(),
                evento.fin(),
                evento.todoElDia(),
                evento.usuarioPeriodoCursoId(),
                evento.cursoId(),
                evento.codigoCurso(),
                evento.nombreCurso(),
                evento.referenciaCodigo()
        );
    }
}
