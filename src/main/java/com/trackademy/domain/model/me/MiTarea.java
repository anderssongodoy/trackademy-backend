package com.trackademy.domain.model.me;

import java.time.OffsetDateTime;

public record MiTarea(
        Long id,
        Long usuarioPeriodoId,
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigoCurso,
        String nombreCurso,
        String titulo,
        String descripcion,
        String tipo,
        String prioridad,
        String estado,
        OffsetDateTime fechaVencimiento,
        OffsetDateTime fechaRecordatorio,
        String canalRecordatorio,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
