package com.trackademy.application.port.out;

import com.trackademy.domain.model.auth.AppPrincipal;

import java.util.Optional;

public interface GoogleIdentityPort {
    Optional<AppPrincipal> verifyGoogleIdToken(String idToken);
}
