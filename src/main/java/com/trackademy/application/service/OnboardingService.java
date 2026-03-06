package com.trackademy.application.service;

import com.trackademy.application.port.in.OnboardingUseCase;
import com.trackademy.application.port.out.OnboardingCommandPort;
import com.trackademy.domain.model.onboarding.OnboardingCommand;
import com.trackademy.domain.model.onboarding.OnboardingResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OnboardingService implements OnboardingUseCase {

    private final OnboardingCommandPort onboardingCommandPort;

    public OnboardingService(OnboardingCommandPort onboardingCommandPort) {
        this.onboardingCommandPort = onboardingCommandPort;
    }

    @Override
    public OnboardingResult completarOnboardingBasico(OnboardingCommand command) {
        return onboardingCommandPort.completarOnboardingBasico(command);
    }
}
