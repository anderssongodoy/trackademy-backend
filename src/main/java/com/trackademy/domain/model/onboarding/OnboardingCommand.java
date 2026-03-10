package com.trackademy.domain.model.onboarding;

import java.math.BigDecimal;
import java.util.List;

public record OnboardingCommand(
        String email,
        String nombre,
        String nombrePreferido,
        String emailInstitucional,
        Long campusId,
        Long periodoId,
        Long carreraId,
        Integer cicloActual,
        BigDecimal metaPromedioCiclo,
        Integer horasEstudioSemanaObjetivo,
        List<CursoSeleccionado> cursos,
        List<FranjaEstudioPreferida> franjasPreferidasEstudio,
        List<ConfianzaCurso> confianzaPorCurso
) {
    public record CursoSeleccionado(
            Long cursoId,
            String seccion,
            String profesor,
            String modalidad,
            List<HorarioCurso> horarios
    ) {
    }

    public record HorarioCurso(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            String tipoSesion,
            String ubicacion,
            String urlVirtual
    ) {
    }

    public record FranjaEstudioPreferida(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            Integer prioridad,
            String tipo
    ) {
    }

    public record ConfianzaCurso(
            Long cursoId,
            Integer nivelConfianza,
            String comentario
    ) {
    }
}
