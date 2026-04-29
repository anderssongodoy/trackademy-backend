package com.trackademy.domain.model.me;

import java.time.OffsetDateTime;

public record GuardarTareaCommand(
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
}
