package com.trackademy.adapter.out.auth;

import com.trackademy.domain.model.auth.AppPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del JwtTokenAdapter.
 *
 * Cubre:
 *   - HU-01 (Login Google / Microsoft): el backend emite y valida un JWT propio.
 *   - Seguridad: rechazo de tokens sin "Bearer ", con firma distinta o issuer distinto.
 *
 * Test plan: SP-001 (JWT propio) + SP-005 (validacion de tokens).
 */
class JwtTokenAdapterTest {

    private static final String SECRET = "test-secret-only-for-unit-tests-1234567890";
    private static final String ISSUER = "trackademy-test";
    private static final long TTL_SECONDS = 3600L;

    private final JwtTokenAdapter adapter = new JwtTokenAdapter(SECRET, ISSUER, TTL_SECONDS);

    @Test
    @DisplayName("createToken emite un JWT no vacio con tres segmentos separados por punto")
    void createToken_returnsValidJwtForPrincipal() {
        AppPrincipal principal = new AppPrincipal("estudiante@trackademy.test", "Estudiante Trackademy");

        String token = adapter.createToken(principal);

        assertNotNull(token, "el token no debe ser nulo");
        assertFalse(token.isBlank(), "el token no debe estar vacio");
        assertEquals(3, token.split("\\.").length, "un JWT valido tiene header.payload.signature");
    }

    @Test
    @DisplayName("fromAuthorizationHeader extrae el principal cuando el header es 'Bearer <token>'")
    void fromAuthorizationHeader_extractsPrincipalFromValidBearerToken() {
        AppPrincipal original = new AppPrincipal("estudiante@trackademy.test", "Estudiante Trackademy");
        String token = adapter.createToken(original);

        Optional<AppPrincipal> result = adapter.fromAuthorizationHeader("Bearer " + token);

        assertTrue(result.isPresent(), "el adapter debe poder reconstruir al principal");
        assertEquals(original.email(), result.get().email());
        assertEquals(original.name(), result.get().name());
    }

    @Test
    @DisplayName("fromAuthorizationHeader rechaza el token cuando falta el prefijo 'Bearer '")
    void fromAuthorizationHeader_returnsEmptyWhenMissingBearerPrefix() {
        AppPrincipal principal = new AppPrincipal("user@trackademy.test", "User");
        String token = adapter.createToken(principal);

        Optional<AppPrincipal> result = adapter.fromAuthorizationHeader(token);

        assertTrue(result.isEmpty(), "sin prefijo Bearer, el token debe rechazarse");
    }

    @Test
    @DisplayName("fromAuthorizationHeader devuelve vacio para header nulo o en blanco")
    void fromAuthorizationHeader_returnsEmptyForNullOrBlankHeader() {
        assertTrue(adapter.fromAuthorizationHeader(null).isEmpty());
        assertTrue(adapter.fromAuthorizationHeader("").isEmpty());
        assertTrue(adapter.fromAuthorizationHeader("Bearer ").isEmpty(), "token vacio tras 'Bearer ' debe rechazarse");
    }

    @Test
    @DisplayName("Seguridad: rechaza tokens firmados con otro secret")
    void fromAuthorizationHeader_returnsEmptyForTokenSignedWithDifferentSecret() {
        JwtTokenAdapter foreignAdapter = new JwtTokenAdapter(
                "otro-secret-completamente-distinto-xyz",
                ISSUER,
                TTL_SECONDS
        );
        AppPrincipal principal = new AppPrincipal("user@trackademy.test", "User");
        String foreignToken = foreignAdapter.createToken(principal);

        Optional<AppPrincipal> result = adapter.fromAuthorizationHeader("Bearer " + foreignToken);

        assertTrue(result.isEmpty(), "un token firmado con otro secret no debe pasar la verificacion");
    }

    @Test
    @DisplayName("Seguridad: rechaza tokens emitidos con otro issuer")
    void fromAuthorizationHeader_returnsEmptyForTokenWithDifferentIssuer() {
        JwtTokenAdapter foreignIssuer = new JwtTokenAdapter(SECRET, "otro-issuer", TTL_SECONDS);
        AppPrincipal principal = new AppPrincipal("user@trackademy.test", "User");
        String foreignToken = foreignIssuer.createToken(principal);

        Optional<AppPrincipal> result = adapter.fromAuthorizationHeader("Bearer " + foreignToken);

        assertTrue(result.isEmpty(), "un token con issuer distinto no debe ser aceptado");
    }

    @Test
    @DisplayName("Seguridad: token manipulado en la firma se rechaza")
    void fromAuthorizationHeader_returnsEmptyForTamperedToken() {
        AppPrincipal principal = new AppPrincipal("user@trackademy.test", "User");
        String original = adapter.createToken(principal);

        String tampered = original.substring(0, original.length() - 4) + "AAAA";

        Optional<AppPrincipal> result = adapter.fromAuthorizationHeader("Bearer " + tampered);

        assertTrue(result.isEmpty(), "un token alterado en su firma debe rechazarse");
    }

    @Test
    @DisplayName("getTtlSeconds devuelve el TTL configurado")
    void getTtlSeconds_returnsConfiguredTtl() {
        assertEquals(TTL_SECONDS, adapter.getTtlSeconds());
    }
}
