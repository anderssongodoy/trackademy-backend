package com.trackademy.domain.model;

import java.time.OffsetDateTime;

public record CursoSilaboVersion(
        Long silaboId,
        String version,
        boolean vigente,
        Integer anio,
        String periodoTexto,
        OffsetDateTime extraidoEn,
        CursoSilaboPdf pdf
) {
}
