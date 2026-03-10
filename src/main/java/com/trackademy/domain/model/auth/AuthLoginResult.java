package com.trackademy.domain.model.auth;

public record AuthLoginResult(
        String token,
        String tokenType,
        long expiresIn,
        String email,
        String name
) {
}
