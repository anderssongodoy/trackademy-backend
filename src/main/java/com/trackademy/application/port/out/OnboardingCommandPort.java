package com.trackademy.application.port.out;

import com.trackademy.domain.model.onboarding.OnboardingCommand;
import com.trackademy.domain.model.onboarding.OnboardingResult;

public interface OnboardingCommandPort {
    OnboardingResult completarOnboardingBasico(OnboardingCommand command);
}
