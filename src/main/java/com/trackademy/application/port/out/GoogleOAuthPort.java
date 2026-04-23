package com.trackademy.application.port.out;

import com.trackademy.domain.model.auth.GoogleOAuthTokenSet;

import java.util.Optional;

public interface GoogleOAuthPort {
    String buildAuthorizationUrl(String state);

    Optional<GoogleOAuthTokenSet> exchangeAuthorizationCode(String code);
}
