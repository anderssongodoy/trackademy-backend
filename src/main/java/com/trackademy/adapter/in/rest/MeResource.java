package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.MiCursoResponse;
import com.trackademy.adapter.in.rest.dto.MiPeriodoActualResponse;
import com.trackademy.application.port.in.MeQueryUseCase;
import com.trackademy.security.AuthTokenService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    private final MeQueryUseCase meQueryUseCase;
    private final AuthTokenService authTokenService;

    public MeResource(MeQueryUseCase meQueryUseCase, AuthTokenService authTokenService) {
        this.meQueryUseCase = meQueryUseCase;
        this.authTokenService = authTokenService;
    }

    @GET
    @Path("/periodo-actual")
    public Response periodoActual(@HeaderParam("Authorization") String authorization) {
        var principal = authTokenService.fromAuthorizationHeader(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();

        return meQueryUseCase.obtenerPeriodoActual(email)
                .map(MiPeriodoActualResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/cursos")
    public Response misCursos(@HeaderParam("Authorization") String authorization) {
        var principal = authTokenService.fromAuthorizationHeader(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();

        List<MiCursoResponse> cursos = meQueryUseCase.listarMisCursos(email).stream()
                .map(MiCursoResponse::from)
                .toList();

        return Response.ok(cursos).build();
    }
}
