package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.ApiErrorResponse;
import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.domain.model.auth.AuthLoginResult;
import com.trackademy.domain.model.auth.AuthSession;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthUseCase authUseCase;
    private final String frontendBaseUrl;

    public AuthResource(
            AuthUseCase authUseCase,
            @ConfigProperty(name = "app.frontend.base-url", defaultValue = "http://localhost:4200") String frontendBaseUrl
    ) {
        this.authUseCase = authUseCase;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @POST
    @Path("/microsoft")
    public Response microsoft(MicrosoftLoginRequest request) {
        if (request == null || request.idToken() == null || request.idToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiErrorResponse.validation("idToken es requerido"))
                    .build();
        }

        return authUseCase.loginWithMicrosoft(request.idToken())
                .map(this::buildLoginResponse)
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiErrorResponse.unauthorized("Token de Microsoft invalido"))
                        .build());
    }

    @POST
    @Path("/google")
    public Response google(GoogleLoginRequest request) {
        if (request == null || request.idToken() == null || request.idToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiErrorResponse.validation("idToken es requerido"))
                    .build();
        }

        return authUseCase.loginWithGoogle(request.idToken())
                .map(this::buildLoginResponse)
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiErrorResponse.unauthorized("Token de Google invalido"))
                        .build());
    }

    @GET
    @Path("/google/oauth-url")
    public Response googleOAuthUrl(@QueryParam("redirectPath") String redirectPath) {
        try {
            return Response.ok(GoogleOAuthUrlResponse.from(authUseCase.startGoogleOAuthLogin(redirectPath).authorizationUrl())).build();
        } catch (IllegalStateException error) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(ApiErrorResponse.validation("Google OAuth no esta configurado"))
                    .build();
        }
    }

    @GET
    @Path("/google/callback")
    public Response googleCallback(
            @QueryParam("code") String code,
            @QueryParam("state") String state,
            @QueryParam("error") String error
    ) {
        if (error != null && !error.isBlank()) {
            return redirectToFrontend("/auth/sign-in", "googleError=" + encode(error));
        }

        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            return redirectToFrontend("/auth/sign-in", "googleError=missing_code");
        }

        return authUseCase.loginWithGoogleAuthorizationCode(code, state)
                .map(result -> redirectToFrontend(
                        result.redirectPath(),
                        "token=" + encode(result.auth().token())
                                + "&expiresIn=" + result.auth().expiresIn()
                                + "&email=" + encode(result.auth().email())
                                + "&name=" + encode(result.auth().name())
                ))
                .orElseGet(() -> redirectToFrontend("/auth/sign-in", "googleError=invalid_oauth"));
    }

    @GET
    @Path("/session")
    public AuthSessionResponse session(@HeaderParam("Authorization") String authorization) {
        AuthSession session = authUseCase.sessionFromAuthorization(authorization);
        return new AuthSessionResponse(session.authenticated(), session.email(), session.name());
    }

    private Response buildLoginResponse(AuthLoginResult result) {
        return Response.ok(new MicrosoftLoginResponse(
                result.token(),
                result.tokenType(),
                result.expiresIn(),
                result.email(),
                result.name()
        )).build();
    }

    private Response redirectToFrontend(String path, String fragment) {
        String safePath = path != null && path.startsWith("/") && !path.startsWith("//") ? path : "/auth/sign-in";
        String base = frontendBaseUrl != null && frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return Response.seeOther(URI.create(base + safePath + "#" + fragment)).build();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public record MicrosoftLoginRequest(
            String idToken
    ) {
    }

    public record GoogleLoginRequest(
            String idToken
    ) {
    }

    public record GoogleOAuthUrlResponse(
            String authorizationUrl
    ) {
        public static GoogleOAuthUrlResponse from(String authorizationUrl) {
            return new GoogleOAuthUrlResponse(authorizationUrl);
        }
    }

    public record MicrosoftLoginResponse(
            String token,
            String tokenType,
            long expiresIn,
            String email,
            String name
    ) {
    }

    public record AuthSessionResponse(
            boolean authenticated,
            String email,
            String name
    ) {
    }
}
