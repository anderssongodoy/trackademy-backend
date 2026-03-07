package com.trackademy.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class AuthTokenService {

    private final Algorithm algorithm;
    private final String issuer;
    private final long ttlSeconds;

    public AuthTokenService(
            @ConfigProperty(name = "app.auth.jwt.secret", defaultValue = "trackademy-local-dev-secret-change-me") String secret,
            @ConfigProperty(name = "app.auth.jwt.issuer", defaultValue = "trackademy") String issuer,
            @ConfigProperty(name = "app.auth.jwt.ttl-seconds", defaultValue = "28800") long ttlSeconds
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(AppPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(principal.email())
                .withClaim("name", principal.name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);
    }

    public Optional<AppPrincipal> verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            DecodedJWT decoded = verifier.verify(token);
            String email = decoded.getSubject();
            String name = decoded.getClaim("name").asString();

            if (email == null || email.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new AppPrincipal(email, name));
        } catch (JWTVerificationException ignored) {
            return Optional.empty();
        }
    }

    public Optional<AppPrincipal> fromAuthorizationHeader(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            return Optional.empty();
        }

        return verifyToken(token);
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}
