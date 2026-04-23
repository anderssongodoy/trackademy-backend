package com.trackademy.domain.model.auth;

import java.time.OffsetDateTime;

public record GoogleOAuthTokenSet(
        String idToken,
        String accessToken,
        String refreshToken,
        OffsetDateTime expiresAt,
        String subject,
        String email,
        String name
) {
}
