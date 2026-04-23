package com.trackademy.application.port.in;

import com.trackademy.domain.model.auth.AppPrincipal;
import com.trackademy.domain.model.auth.AuthLoginResult;
import com.trackademy.domain.model.auth.AuthSession;
import com.trackademy.domain.model.auth.GoogleOAuthLoginResult;
import com.trackademy.domain.model.auth.GoogleOAuthStartResult;

import java.util.Optional;

public interface AuthUseCase {
    Optional<AuthLoginResult> loginWithGoogle(String idToken);

    GoogleOAuthStartResult startGoogleOAuthLogin(String redirectPath);

    Optional<GoogleOAuthLoginResult> loginWithGoogleAuthorizationCode(String code, String state);

    Optional<AuthLoginResult> loginWithMicrosoft(String idToken);

    Optional<AppPrincipal> authenticate(String authorizationHeader);

    AuthSession sessionFromAuthorization(String authorizationHeader);
}
