package com.trackademy.domain.model;

public record CursoSilaboDownload(
        Long silaboId,
        String storageProvider,
        String storageKey,
        String filename,
        String mimeType
) {
}
