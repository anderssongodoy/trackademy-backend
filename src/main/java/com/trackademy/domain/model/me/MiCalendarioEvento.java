package com.trackademy.domain.model.me;

import java.time.LocalDateTime;

public record MiCalendarioEvento(
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
}
