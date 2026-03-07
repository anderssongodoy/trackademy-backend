package com.trackademy.security;

public record AppPrincipal(
        String email,
        String name
) {
}
