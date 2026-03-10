package com.trackademy.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record OnboardingRequest(
        String nombre,
        String nombrePreferido,
        String emailInstitucional,
        Long campusId,
        Long periodoId,
        Long carreraId,
        Integer cicloActual,
        BigDecimal metaPromedioCiclo,
        Integer horasEstudioSemanaObjetivo,
        List<CursoSeleccionadoRequest> cursos,
        List<FranjaEstudioPreferidaRequest> franjasPreferidasEstudio,
        List<ConfianzaCursoRequest> confianzaPorCurso
) {
    public record CursoSeleccionadoRequest(
            Long cursoId,
            String seccion,
            String profesor,
            String modalidad,
            List<HorarioCursoRequest> horarios
    ) {
    }

    public record HorarioCursoRequest(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            String tipoSesion,
            String ubicacion,
            String urlVirtual
    ) {
    }

    public record FranjaEstudioPreferidaRequest(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            Integer prioridad,
            String tipo
    ) {
    }

    public record ConfianzaCursoRequest(
            Long cursoId,
            Integer nivelConfianza,
            String comentario
    ) {
    }
}
