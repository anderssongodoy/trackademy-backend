package com.trackademy.domain.model.onboarding;

public record OnboardingResult(
        Long usuarioId,
        Long usuarioPeriodoId,
        Integer cursosRegistrados,
        Integer horariosRegistrados,
        Integer franjasRegistradas,
        Integer confianzasRegistradas
) {
}
