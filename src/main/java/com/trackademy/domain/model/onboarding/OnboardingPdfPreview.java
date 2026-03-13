package com.trackademy.domain.model.onboarding;

import java.util.List;

public record OnboardingPdfPreview(
        String codigoAlumno,
        String nombreCompleto,
        String emailInstitucional,
        Long carreraId,
        String carreraNombre,
        Long campusId,
        String campusNombre,
        String campusTexto,
        Long periodoId,
        String periodoEtiqueta,
        String periodoTexto,
        Integer cicloActual,
        List<CursoDetectado> cursosDetectados,
        List<String> advertencias
) {
    public record CursoDetectado(
            Long cursoId,
            String codigo,
            String nombre,
            String profesor,
            String seccion,
            String modalidad,
            List<BloqueHorario> horarios
    ) {
    }

    public record BloqueHorario(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            String descripcion
    ) {
    }
}
