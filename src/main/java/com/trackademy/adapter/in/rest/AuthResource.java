package com.trackademy.adapter.in.rest;

import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.domain.model.auth.AuthLoginResult;
import com.trackademy.domain.model.auth.AuthSession;
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

    private final AuthUseCase authUseCase;

    public AuthResource(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @POST
    @Path("/microsoft")
    public Response microsoft(MicrosoftLoginRequest request) {
    if (request == null || request.idToken() == null || request.idToken().isBlank()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("idToken es requerido")
            .build();
    }

    return authUseCase.loginWithMicrosoft(request.idToken())
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

            return authUseCase.loginWithGoogle(request.idToken())
                .map(this::buildLoginResponse)
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token de Google invalido")
                    .build());
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
