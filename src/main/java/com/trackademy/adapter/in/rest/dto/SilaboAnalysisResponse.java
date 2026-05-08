package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.SilaboAnalysis;

import java.time.OffsetDateTime;
import java.util.List;

public record SilaboAnalysisResponse(
        Long silaboId,
        String resumen,
        List<String> temas,
        List<RecursoResponse> recursos,
        OffsetDateTime generatedAt
) {

    public record RecursoResponse(
            String titulo,
            String tipo,
            String url,
            String descripcion
    ) {}

    public static SilaboAnalysisResponse from(SilaboAnalysis analysis) {
        return new SilaboAnalysisResponse(
                analysis.silaboId(),
                analysis.resumen(),
                analysis.temas(),
                analysis.recursos().stream()
                        .map(r -> new RecursoResponse(r.titulo(), r.tipo(), r.url(), r.descripcion()))
                        .toList(),
                analysis.generatedAt()
        );
    }
}
