package com.trackademy.application.port.in;

import com.trackademy.domain.model.auth.AppPrincipal;
import com.trackademy.domain.model.auth.AuthLoginResult;
import com.trackademy.domain.model.auth.AuthSession;

import java.util.Optional;

public interface AuthUseCase {
    Optional<AuthLoginResult> loginWithGoogle(String idToken);

    Optional<AuthLoginResult> loginWithMicrosoft(String idToken);

    Optional<AppPrincipal> authenticate(String authorizationHeader);

    AuthSession sessionFromAuthorization(String authorizationHeader);
}
