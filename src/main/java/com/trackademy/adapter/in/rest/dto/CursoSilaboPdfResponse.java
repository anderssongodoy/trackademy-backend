package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.CursoSilaboPdf;

public record CursoSilaboPdfResponse(
        Long assetId,
        String originalFilename,
        String sourceFilename,
        String mimeType,
        Long sizeBytes,
        String sha256,
        String storageProvider,
        boolean disponibleDescarga
) {
    public static CursoSilaboPdfResponse from(CursoSilaboPdf pdf) {
        return new CursoSilaboPdfResponse(
                pdf.assetId(),
                pdf.originalFilename(),
                pdf.sourceFilename(),
                pdf.mimeType(),
                pdf.sizeBytes(),
                pdf.sha256(),
                pdf.storageProvider(),
                pdf.disponibleDescarga()
        );
    }
}
