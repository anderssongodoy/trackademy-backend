package com.trackademy.application.service;

import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.application.port.out.AuthPersistencePort;
import com.trackademy.application.port.out.AuthTokenPort;
import com.trackademy.application.port.out.GoogleIdentityPort;
import com.trackademy.application.port.out.GoogleOAuthPort;
import com.trackademy.application.port.out.MicrosoftIdentityPort;
import com.trackademy.domain.model.auth.AppPrincipal;
import com.trackademy.domain.model.auth.AuthLoginResult;
import com.trackademy.domain.model.auth.AuthSession;
import com.trackademy.domain.model.auth.GoogleOAuthLoginResult;
import com.trackademy.domain.model.auth.GoogleOAuthStartResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@ApplicationScoped
public class AuthService implements AuthUseCase {

    private static final Logger LOG = Logger.getLogger(AuthService.class);
    private static final long OAUTH_STATE_TTL_SECONDS = 600;

    private final GoogleIdentityPort googleIdentityPort;
    private final GoogleOAuthPort googleOAuthPort;
    private final MicrosoftIdentityPort microsoftIdentityPort;
    private final AuthTokenPort authTokenPort;
    private final AuthPersistencePort authPersistencePort;
    private final String stateSecret;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            GoogleIdentityPort googleIdentityPort,
            GoogleOAuthPort googleOAuthPort,
            MicrosoftIdentityPort microsoftIdentityPort,
            AuthTokenPort authTokenPort,
            AuthPersistencePort authPersistencePort,
            @ConfigProperty(name = "app.auth.jwt.secret", defaultValue = "trackademy-local-dev-secret-change-me") String stateSecret
    ) {
        this.googleIdentityPort = googleIdentityPort;
        this.googleOAuthPort = googleOAuthPort;
        this.microsoftIdentityPort = microsoftIdentityPort;
        this.authTokenPort = authTokenPort;
        this.authPersistencePort = authPersistencePort;
        this.stateSecret = stateSecret;
    }

    @Override
    public Optional<AuthLoginResult> loginWithGoogle(String idToken) {
        return googleIdentityPort.verifyGoogleIdToken(idToken)
                .map(this::buildLoginResult);
    }

    @Override
    public GoogleOAuthStartResult startGoogleOAuthLogin(String redirectPath) {
        String safeRedirectPath = sanitizeRedirectPath(redirectPath);
        String state = encodeState(safeRedirectPath);
        return new GoogleOAuthStartResult(googleOAuthPort.buildAuthorizationUrl(state));
    }

    @Override
    public Optional<GoogleOAuthLoginResult> loginWithGoogleAuthorizationCode(String code, String state) {
        Optional<String> redirectPathOpt = decodeState(state);
        if (redirectPathOpt.isEmpty()) {
            return Optional.empty();
        }

        return googleOAuthPort.exchangeAuthorizationCode(code)
                .flatMap(tokens -> googleIdentityPort.verifyGoogleIdToken(tokens.idToken())
                        .map(principal -> {
                            try {
                                authPersistencePort.upsertGoogleUserAndCalendar(principal, tokens);
                            } catch (RuntimeException error) {
                                LOG.warn("No se pudo guardar la vinculacion de Google Calendar durante el login OAuth", error);
                            }
                            return new GoogleOAuthLoginResult(buildLoginResult(principal), redirectPathOpt.get());
                        }));
    }

    @Override
    public Optional<AuthLoginResult> loginWithMicrosoft(String idToken) {
        return microsoftIdentityPort.verifyMicrosoftIdToken(idToken)
                .map(this::buildLoginResult);
    }

    @Override
    public Optional<AppPrincipal> authenticate(String authorizationHeader) {
        return authTokenPort.fromAuthorizationHeader(authorizationHeader);
    }

    @Override
    public AuthSession sessionFromAuthorization(String authorizationHeader) {
        return authTokenPort.fromAuthorizationHeader(authorizationHeader)
                .map(principal -> new AuthSession(true, principal.email(), principal.name()))
                .orElse(new AuthSession(false, null, null));
    }

    private AuthLoginResult buildLoginResult(AppPrincipal principal) {
        String token = authTokenPort.createToken(principal);
        return new AuthLoginResult(
                token,
                "Bearer",
                authTokenPort.getTtlSeconds(),
                principal.email(),
                principal.name()
        );
    }

    private String sanitizeRedirectPath(String redirectPath) {
        if (redirectPath == null || redirectPath.isBlank() || !redirectPath.startsWith("/") || redirectPath.startsWith("//")) {
            return "/auth/sign-in";
        }
        return redirectPath;
    }

    private String encodeState(String redirectPath) {
        long issuedAt = Instant.now().getEpochSecond();
        byte[] nonceBytes = new byte[18];
        secureRandom.nextBytes(nonceBytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
        String payload = redirectPath + "\n" + issuedAt + "\n" + nonce;
        String signature = sign(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "\n" + signature).getBytes(StandardCharsets.UTF_8));
    }

    private Optional<String> decodeState(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\n", 4);
            if (parts.length != 4) {
                return Optional.empty();
            }
            String payload = parts[0] + "\n" + parts[1] + "\n" + parts[2];
            String expectedSignature = sign(payload);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[3].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            long issuedAt = Long.parseLong(parts[1]);
            long age = Instant.now().getEpochSecond() - issuedAt;
            if (age < 0 || age > OAUTH_STATE_TTL_SECONDS) {
                return Optional.empty();
            }
            return Optional.of(sanitizeRedirectPath(parts[0]));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(stateSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar estado OAuth", e);
        }
    }
}
