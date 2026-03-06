package com.trackademy.application.port.in;

import com.trackademy.domain.model.onboarding.OnboardingCommand;
import com.trackademy.domain.model.onboarding.OnboardingResult;

public interface OnboardingUseCase {
    OnboardingResult completarOnboardingBasico(OnboardingCommand command);
}
