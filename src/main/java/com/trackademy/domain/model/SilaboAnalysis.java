package com.trackademy.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

public record SilaboAnalysis(
        Long silaboId,
        String hashPdf,
        String resumen,
        List<String> temas,
        List<SilaboAnalysisRecurso> recursos,
        List<String> paraIrMasAlla,
        Integer promptTokens,
        Integer completionTokens,
        OffsetDateTime generatedAt
) {
}
