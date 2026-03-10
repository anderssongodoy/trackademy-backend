package com.trackademy.adapter.out.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.trackademy.application.port.out.MicrosoftIdentityPort;
import com.trackademy.domain.model.auth.AppPrincipal;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MicrosoftIdentityAdapter implements MicrosoftIdentityPort {

    private final String frontendClientId;
    private final String issuerPrefix;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public MicrosoftIdentityAdapter(
            @ConfigProperty(name = "app.auth.microsoft.frontend-client-id", defaultValue = "") String frontendClientId,
            @ConfigProperty(name = "app.auth.microsoft.jwks-uri", defaultValue = "https://login.microsoftonline.com/common/discovery/v2.0/keys") String jwksUri,
            @ConfigProperty(name = "app.auth.microsoft.issuer-prefix", defaultValue = "https://login.microsoftonline.com/") String issuerPrefix
    ) throws Exception {
        this.frontendClientId = frontendClientId;
        this.issuerPrefix = issuerPrefix;

        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUri));
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

        jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);
    }

    @Override
    public Optional<AppPrincipal> verifyMicrosoftIdToken(String idToken) {
        if (frontendClientId == null || frontendClientId.isBlank()) {
            return Optional.empty();
        }

        try {
            JWTClaimsSet claims = jwtProcessor.process(idToken, null);

            if (!isAudienceValid(claims)) {
                return Optional.empty();
            }

            if (!isIssuerValid(claims)) {
                return Optional.empty();
            }

            if (!isNotExpired(claims)) {
                return Optional.empty();
            }

            String email = firstNonBlank(
                    claims.getStringClaim("email"),
                    claims.getStringClaim("preferred_username"),
                    claims.getSubject()
            );

            if (email == null || email.isBlank()) {
                return Optional.empty();
            }

            String name = firstNonBlank(claims.getStringClaim("name"), email, email);
            return Optional.of(new AppPrincipal(email, name));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private boolean isAudienceValid(JWTClaimsSet claims) {
        List<String> aud = claims.getAudience();
        return aud != null && aud.contains(frontendClientId);
    }

    private boolean isIssuerValid(JWTClaimsSet claims) {
        String issuer = claims.getIssuer();
        if (issuer == null || issuerPrefix == null) {
            return false;
        }

        return issuer.startsWith(issuerPrefix);
    }

    private boolean isNotExpired(JWTClaimsSet claims) {
        if (claims.getExpirationTime() == null) {
            return false;
        }

        return claims.getExpirationTime().toInstant().isAfter(Instant.now());
    }

    private String firstNonBlank(String first, String second, String third) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        if (third != null && !third.isBlank()) {
            return third;
        }

        return null;
    }
}
