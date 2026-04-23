package com.trackademy.application.port.out;

import com.trackademy.domain.model.auth.AppPrincipal;
import com.trackademy.domain.model.auth.GoogleOAuthTokenSet;

public interface AuthPersistencePort {
    void upsertGoogleUserAndCalendar(AppPrincipal principal, GoogleOAuthTokenSet tokens);
}
