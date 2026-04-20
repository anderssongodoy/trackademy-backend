package com.trackademy.domain.model;

public record CursoSilaboPdf(
        Long assetId,
        String originalFilename,
        String sourceFilename,
        String mimeType,
        Long sizeBytes,
        String sha256,
        String storageProvider,
        boolean disponibleDescarga
) {
}
