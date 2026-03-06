package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.CursoResponse;
import com.trackademy.application.port.in.CatalogoCursosUseCase;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/catalog/cursos")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoCursosResource {

    private final CatalogoCursosUseCase catalogoCursosUseCase;

    public CatalogoCursosResource(CatalogoCursosUseCase catalogoCursosUseCase) {
        this.catalogoCursosUseCase = catalogoCursosUseCase;
    }

    @GET
    public List<CursoResponse> listar() {
        return catalogoCursosUseCase.listarCursos().stream()
                .map(CursoResponse::from)
                .toList();
    }

    @GET
    @Path("/{codigo}")
    public Response obtenerPorCodigo(@PathParam("codigo") String codigo) {
        return catalogoCursosUseCase.obtenerPorCodigo(codigo)
                .map(CursoResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
