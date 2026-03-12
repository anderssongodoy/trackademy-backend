package com.trackademy.domain.model.me;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MiPeriodoActual(
        Long usuarioId,
        Long usuarioPeriodoId,
        Long periodoId,
        Long campusId,
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
