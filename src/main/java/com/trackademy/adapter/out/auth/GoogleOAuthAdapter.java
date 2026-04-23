package com.trackademy.adapter.out.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.application.port.out.GoogleOAuthPort;
import com.trackademy.domain.model.auth.GoogleOAuthTokenSet;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class GoogleOAuthAdapter implements GoogleOAuthPort {

    private static final String CALENDAR_EVENTS_SCOPE = "https://www.googleapis.com/auth/calendar.events";
    private static final String BASE_SCOPES = "openid email profile " + CALENDAR_EVENTS_SCOPE;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authorizationUri;
    private final String tokenUri;

    public GoogleOAuthAdapter(
            ObjectMapper objectMapper,
            @ConfigProperty(name = "app.auth.google.frontend-client-id", defaultValue = "") String clientId,
            @ConfigProperty(name = "app.auth.google.client-secret", defaultValue = "") String clientSecret,
            @ConfigProperty(name = "app.auth.google.redirect-uri", defaultValue = "") String redirectUri,
            @ConfigProperty(name = "app.auth.google.authorization-uri", defaultValue = "https://accounts.google.com/o/oauth2/v2/auth") String authorizationUri,
            @ConfigProperty(name = "app.auth.google.token-uri", defaultValue = "https://oauth2.googleapis.com/token") String tokenUri
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.authorizationUri = authorizationUri;
        this.tokenUri = tokenUri;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        ensureConfigured();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("client_id", clientId);
        params.put("redirect_uri", redirectUri);
        params.put("response_type", "code");
        params.put("scope", BASE_SCOPES);
        params.put("access_type", "offline");
        params.put("include_granted_scopes", "true");
        params.put("prompt", "consent");
        params.put("state", state);
        return authorizationUri + "?" + formEncode(params);
    }

    @Override
    public Optional<GoogleOAuthTokenSet> exchangeAuthorizationCode(String code) {
        ensureConfigured();
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        try {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("code", code);
            form.put("client_id", clientId);
            form.put("client_secret", clientSecret);
            form.put("redirect_uri", redirectUri);
            form.put("grant_type", "authorization_code");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUri))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formEncode(form)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode json = objectMapper.readTree(response.body());
            String idToken = text(json, "id_token");
            String accessToken = text(json, "access_token");
            String refreshToken = text(json, "refresh_token");
            long expiresIn = json.path("expires_in").asLong(0L);

            if (idToken == null || accessToken == null) {
                return Optional.empty();
            }

            DecodedJWT decoded = JWT.decode(idToken);
            String email = decoded.getClaim("email").asString();
            String name = decoded.getClaim("name").asString();
            String subject = decoded.getSubject();

            return Optional.of(new GoogleOAuthTokenSet(
                    idToken,
                    accessToken,
                    refreshToken,
                    expiresIn > 0 ? OffsetDateTime.now().plusSeconds(expiresIn) : null,
                    subject,
                    email,
                    name
            ));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private void ensureConfigured() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank() || redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalStateException("Google OAuth no esta configurado");
        }
    }

    private String formEncode(Map<String, String> values) {
        StringJoiner joiner = new StringJoiner("&");
        values.forEach((key, value) -> joiner.add(urlEncode(key) + "=" + urlEncode(value)));
        return joiner.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode json, String field) {
        JsonNode value = json.path(field);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }
}
