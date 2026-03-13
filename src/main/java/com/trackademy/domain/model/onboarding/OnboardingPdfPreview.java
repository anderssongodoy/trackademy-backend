package com.trackademy.domain.model.onboarding;

import java.util.List;

public record OnboardingPdfPreview(
        Long carreraId,
        String carreraNombre,
        Long campusId,
        String campusNombre,
        Long periodoId,
        String periodoEtiqueta,
        Integer cicloActual,
        List<CursoDetectado> cursosDetectados,
        List<String> advertencias
) {
    public record CursoDetectado(
            Long cursoId,
            String codigo,
            String nombre
    ) {
    }
}
