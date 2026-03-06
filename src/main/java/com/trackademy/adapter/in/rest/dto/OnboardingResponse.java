package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.onboarding.OnboardingResult;

public record OnboardingResponse(
        Long usuarioId,
        Long usuarioPeriodoId,
        Integer cursosRegistrados,
        Integer horariosRegistrados,
        Integer franjasRegistradas,
        Integer confianzasRegistradas
) {
    public static OnboardingResponse from(OnboardingResult result) {
        return new OnboardingResponse(
                result.usuarioId(),
                result.usuarioPeriodoId(),
                result.cursosRegistrados(),
                result.horariosRegistrados(),
                result.franjasRegistradas(),
                result.confianzasRegistradas()
        );
    }
}
