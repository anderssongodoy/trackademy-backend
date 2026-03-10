package com.trackademy.application.service;

import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.application.port.out.AuthTokenPort;
import com.trackademy.application.port.out.GoogleIdentityPort;
import com.trackademy.application.port.out.MicrosoftIdentityPort;
import com.trackademy.domain.model.auth.AppPrincipal;
import com.trackademy.domain.model.auth.AuthLoginResult;
import com.trackademy.domain.model.auth.AuthSession;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AuthService implements AuthUseCase {

    private final GoogleIdentityPort googleIdentityPort;
    private final MicrosoftIdentityPort microsoftIdentityPort;
    private final AuthTokenPort authTokenPort;

    public AuthService(
            GoogleIdentityPort googleIdentityPort,
            MicrosoftIdentityPort microsoftIdentityPort,
            AuthTokenPort authTokenPort
    ) {
        this.googleIdentityPort = googleIdentityPort;
        this.microsoftIdentityPort = microsoftIdentityPort;
        this.authTokenPort = authTokenPort;
    }

    @Override
    public Optional<AuthLoginResult> loginWithGoogle(String idToken) {
        return googleIdentityPort.verifyGoogleIdToken(idToken)
                .map(this::buildLoginResult);
    }

    @Override
    public Optional<AuthLoginResult> loginWithMicrosoft(String idToken) {
        return microsoftIdentityPort.verifyMicrosoftIdToken(idToken)
                .map(this::buildLoginResult);
    }

    @Override
    public Optional<AppPrincipal> authenticate(String authorizationHeader) {
        return authTokenPort.fromAuthorizationHeader(authorizationHeader);
    }

    @Override
    public AuthSession sessionFromAuthorization(String authorizationHeader) {
        return authTokenPort.fromAuthorizationHeader(authorizationHeader)
                .map(principal -> new AuthSession(true, principal.email(), principal.name()))
                .orElse(new AuthSession(false, null, null));
    }

    private AuthLoginResult buildLoginResult(AppPrincipal principal) {
        String token = authTokenPort.createToken(principal);
        return new AuthLoginResult(
                token,
                "Bearer",
                authTokenPort.getTtlSeconds(),
                principal.email(),
                principal.name()
        );
    }
}
