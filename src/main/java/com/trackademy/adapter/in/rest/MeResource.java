package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.MiCursoResponse;
import com.trackademy.adapter.in.rest.dto.MiPeriodoActualResponse;
import com.trackademy.application.port.in.MeQueryUseCase;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    private final MeQueryUseCase meQueryUseCase;

    public MeResource(MeQueryUseCase meQueryUseCase) {
        this.meQueryUseCase = meQueryUseCase;
    }

    @GET
    @Path("/periodo-actual")
    public Response periodoActual(@QueryParam("email") String email) {
        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("email es requerido")
                    .build();
        }

        return meQueryUseCase.obtenerPeriodoActual(email)
                .map(MiPeriodoActualResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/cursos")
    public Response misCursos(@QueryParam("email") String email) {
        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("email es requerido")
                    .build();
        }

        List<MiCursoResponse> cursos = meQueryUseCase.listarMisCursos(email).stream()
                .map(MiCursoResponse::from)
                .toList();

        return Response.ok(cursos).build();
    }
}
