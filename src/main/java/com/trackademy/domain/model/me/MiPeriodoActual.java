package com.trackademy.domain.model.me;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MiPeriodoActual(
        Long usuarioId,
        String nombre,
        String nombrePreferido,
        String emailInstitucional,
        Long usuarioPeriodoId,
        Long periodoId,
        Long campusId,
        String campusNombre,
        Long carreraId,
        Integer cicloActual,
        String onboardingEstado,
        OffsetDateTime onboardingCompletadoAt,
        BigDecimal metaPromedioCiclo,
        Integer horasEstudioSemanaObjetivo,
        String periodoEtiqueta,
        LocalDate periodoFechaInicio,
        LocalDate periodoFechaFin
) {
}
