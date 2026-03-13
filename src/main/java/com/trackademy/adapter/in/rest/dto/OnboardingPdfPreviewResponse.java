package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.onboarding.OnboardingPdfPreview;

import java.util.List;

public record OnboardingPdfPreviewResponse(
        Long carreraId,
        String carreraNombre,
        Long campusId,
        String campusNombre,
        Long periodoId,
        String periodoEtiqueta,
        Integer cicloActual,
        List<CursoDetectadoResponse> cursosDetectados,
        List<String> advertencias
) {
    public static OnboardingPdfPreviewResponse from(OnboardingPdfPreview preview) {
        return new OnboardingPdfPreviewResponse(
                preview.carreraId(),
                preview.carreraNombre(),
                preview.campusId(),
                preview.campusNombre(),
                preview.periodoId(),
                preview.periodoEtiqueta(),
                preview.cicloActual(),
                preview.cursosDetectados().stream().map(CursoDetectadoResponse::from).toList(),
                preview.advertencias()
        );
    }

    public record CursoDetectadoResponse(
            Long cursoId,
            String codigo,
            String nombre
    ) {
        public static CursoDetectadoResponse from(OnboardingPdfPreview.CursoDetectado curso) {
            return new CursoDetectadoResponse(curso.cursoId(), curso.codigo(), curso.nombre());
        }
    }
}
