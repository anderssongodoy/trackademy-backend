package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.CursoSilaboVersion;

import java.time.OffsetDateTime;

public record CursoSilaboVersionResponse(
        Long silaboId,
        String version,
        boolean vigente,
        Integer anio,
        String periodoTexto,
        OffsetDateTime extraidoEn,
        CursoSilaboPdfResponse pdf,
        String pdfDownloadPath
) {
    public static CursoSilaboVersionResponse from(CursoSilaboVersion version) {
        return new CursoSilaboVersionResponse(
                version.silaboId(),
                version.version(),
                version.vigente(),
                version.anio(),
                version.periodoTexto(),
                version.extraidoEn(),
                version.pdf() == null ? null : CursoSilaboPdfResponse.from(version.pdf()),
                version.pdf() != null && version.pdf().disponibleDescarga()
                        ? "/api/v1/catalog/cursos/silabos/" + version.silaboId() + "/pdf"
                        : null
        );
    }
}
