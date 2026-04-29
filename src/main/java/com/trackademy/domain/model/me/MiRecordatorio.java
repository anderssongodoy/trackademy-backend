package com.trackademy.domain.model.me;

import java.time.OffsetDateTime;

public record MiRecordatorio(
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
}
