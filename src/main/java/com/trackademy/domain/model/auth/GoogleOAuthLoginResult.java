package com.trackademy.domain.model.auth;

public record GoogleOAuthLoginResult(
        AuthLoginResult auth,
        String redirectPath
) {
}
