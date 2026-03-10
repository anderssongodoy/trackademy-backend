package com.trackademy.application.port.out;

import com.trackademy.domain.model.auth.AppPrincipal;

import java.util.Optional;

public interface AuthTokenPort {
    String createToken(AppPrincipal principal);

    Optional<AppPrincipal> fromAuthorizationHeader(String authorizationHeader);

    long getTtlSeconds();
}
