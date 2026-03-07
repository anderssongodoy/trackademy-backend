package com.trackademy.adapter.in.rest;

import com.trackademy.security.AppPrincipal;
import com.trackademy.security.AuthTokenService;
import com.trackademy.security.GoogleIdentityService;
import com.trackademy.security.MicrosoftIdentityService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final MicrosoftIdentityService microsoftIdentityService;
    private final GoogleIdentityService googleIdentityService;
    private final AuthTokenService authTokenService;

    public AuthResource(
        MicrosoftIdentityService microsoftIdentityService,
        GoogleIdentityService googleIdentityService,
        AuthTokenService authTokenService
    ) {
    this.microsoftIdentityService = microsoftIdentityService;
    this.googleIdentityService = googleIdentityService;
    this.authTokenService = authTokenService;
    }

    @POST
    @Path("/microsoft")
    public Response microsoft(MicrosoftLoginRequest request) {
    if (request == null || request.idToken() == null || request.idToken().isBlank()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("idToken es requerido")
            .build();
    }

    return microsoftIdentityService.verifyMicrosoftIdToken(request.idToken())
        .map(this::buildLoginResponse)
        .orElse(Response.status(Response.Status.UNAUTHORIZED)
            .entity("Token de Microsoft invalido")
            .build());
    }

            @POST
            @Path("/google")
            public Response google(GoogleLoginRequest request) {
            if (request == null || request.idToken() == null || request.idToken().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("idToken es requerido")
                    .build();
            }

            return googleIdentityService.verifyGoogleIdToken(request.idToken())
                .map(this::buildLoginResponse)
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token de Google invalido")
                    .build());
            }

    @GET
    @Path("/session")
    public AuthSessionResponse session(@HeaderParam("Authorization") String authorization) {
    return authTokenService.fromAuthorizationHeader(authorization)
        .map(principal -> new AuthSessionResponse(true, principal.email(), principal.name()))
        .orElse(new AuthSessionResponse(false, null, null));
    }

    private Response buildLoginResponse(AppPrincipal principal) {
    String token = authTokenService.createToken(principal);

    return Response.ok(new MicrosoftLoginResponse(
        token,
        "Bearer",
        authTokenService.getTtlSeconds(),
        principal.email(),
        principal.name()
    )).build();
    }

    public record MicrosoftLoginRequest(
        String idToken
    ) {
    }

    public record GoogleLoginRequest(
            String idToken
    ) {
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
