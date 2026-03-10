package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.CursoDetalleResponse;
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
    public List<CursoResponse> listar(@QueryParam("carreraId") Long carreraId,
                                      @QueryParam("q") String query,
                                      @QueryParam("limit") Integer limit,
                                      @QueryParam("offset") Integer offset) {
        boolean hasSearch = query != null || limit != null || offset != null;
        List<CursoResponse> cursos = (hasSearch
                        ? catalogoCursosUseCase.buscarCursos(carreraId, query, limit, offset)
                        : (carreraId == null
                            ? catalogoCursosUseCase.listarCursos()
                            : catalogoCursosUseCase.listarCursosPorCarrera(carreraId)))
                .stream()
                .map(CursoResponse::from)
                .toList();
        return cursos;
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

    @GET
    @Path("/{codigo}/detalle")
    public Response obtenerDetallePorCodigo(@PathParam("codigo") String codigo) {
        return catalogoCursosUseCase.obtenerDetallePorCodigo(codigo)
                .map(CursoDetalleResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
}
