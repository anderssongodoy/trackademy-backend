package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiPeriodoActual;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MiPeriodoActualResponse(
        Long usuarioId,
        Long usuarioPeriodoId,
        Long periodoId,
        Long campusId,
        Long carreraId,
        Integer cicloActual,
        String onboardingEstado,
        OffsetDateTime onboardingCompletadoAt,
        BigDecimal metaPromedioCiclo,
        Integer horasEstudioSemanaObjetivo
) {
    public static MiPeriodoActualResponse from(MiPeriodoActual m) {
        return new MiPeriodoActualResponse(
                m.usuarioId(),
                m.usuarioPeriodoId(),
                m.periodoId(),
                m.campusId(),
                m.carreraId(),
                m.cicloActual(),
                m.onboardingEstado(),
                m.onboardingCompletadoAt(),
                m.metaPromedioCiclo(),
                m.horasEstudioSemanaObjetivo()
        );
    }
}
