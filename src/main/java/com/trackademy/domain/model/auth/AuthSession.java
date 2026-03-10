package com.trackademy.domain.model.auth;

public record AuthSession(
        boolean authenticated,
        String email,
        String name
) {
}
