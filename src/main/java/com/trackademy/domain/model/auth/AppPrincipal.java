package com.trackademy.domain.model.auth;

public record AppPrincipal(
        String email,
        String name
) {
}
