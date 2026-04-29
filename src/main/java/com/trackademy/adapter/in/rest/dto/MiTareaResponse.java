package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiTarea;

import java.time.OffsetDateTime;

public record MiTareaResponse(
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
    public static MiTareaResponse from(MiTarea tarea) {
        return new MiTareaResponse(
                tarea.id(),
                tarea.usuarioPeriodoId(),
                tarea.usuarioPeriodoCursoId(),
                tarea.cursoId(),
                tarea.codigoCurso(),
                tarea.nombreCurso(),
                tarea.titulo(),
                tarea.descripcion(),
                tarea.tipo(),
                tarea.prioridad(),
                tarea.estado(),
                tarea.fechaVencimiento(),
                tarea.fechaRecordatorio(),
                tarea.canalRecordatorio(),
                tarea.completedAt(),
                tarea.createdAt(),
                tarea.updatedAt()
        );
    }
}
