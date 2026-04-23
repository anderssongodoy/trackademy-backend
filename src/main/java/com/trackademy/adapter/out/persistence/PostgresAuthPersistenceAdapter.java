package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.auth.SecureTokenCipher;
import com.trackademy.adapter.out.persistence.entity.CalendarSyncAccountEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.repository.CalendarSyncAccountPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.application.port.out.AuthPersistencePort;
import com.trackademy.domain.model.auth.AppPrincipal;
import com.trackademy.domain.model.auth.GoogleOAuthTokenSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

@ApplicationScoped
public class PostgresAuthPersistenceAdapter implements AuthPersistencePort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final CalendarSyncAccountPanacheRepository calendarSyncAccountRepository;
    private final SecureTokenCipher tokenCipher;

    public PostgresAuthPersistenceAdapter(
            UsuarioPanacheRepository usuarioRepository,
            CalendarSyncAccountPanacheRepository calendarSyncAccountRepository,
            SecureTokenCipher tokenCipher
    ) {
        this.usuarioRepository = usuarioRepository;
        this.calendarSyncAccountRepository = calendarSyncAccountRepository;
        this.tokenCipher = tokenCipher;
    }

    @Override
    @Transactional
    public void upsertGoogleUserAndCalendar(AppPrincipal principal, GoogleOAuthTokenSet tokens) {
        String email = normalize(principal.email());
        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email).orElseGet(() -> {
            UsuarioEntity nuevo = new UsuarioEntity();
            nuevo.email = email;
            nuevo.nombre = principal.name();
            usuarioRepository.persist(nuevo);
            return nuevo;
        });

        if ((usuario.nombre == null || usuario.nombre.isBlank()) && principal.name() != null && !principal.name().isBlank()) {
            usuario.nombre = principal.name();
        }

        CalendarSyncAccountEntity account = calendarSyncAccountRepository
                .buscarPorUsuarioYProvider(usuario.id, "google")
                .orElseGet(() -> {
                    CalendarSyncAccountEntity nuevo = new CalendarSyncAccountEntity();
                    nuevo.usuarioId = usuario.id;
                    nuevo.provider = "google";
                    nuevo.syncDirection = "write";
                    calendarSyncAccountRepository.persist(nuevo);
                    return nuevo;
                });

        account.externalAccountId = tokens.subject();
        account.email = normalize(tokens.email());
        account.calendarId = "primary";
        account.syncDirection = "write";
        account.accessTokenEncrypted = tokenCipher.encrypt(tokens.accessToken());
        if (tokens.refreshToken() != null && !tokens.refreshToken().isBlank()) {
            account.refreshTokenEncrypted = tokenCipher.encrypt(tokens.refreshToken());
        }
        account.tokenExpiresAt = tokens.expiresAt();
        account.estado = "active";
        account.lastSyncAt = OffsetDateTime.now();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
