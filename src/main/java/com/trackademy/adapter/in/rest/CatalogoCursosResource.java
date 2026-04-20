package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.CursoDetalleResponse;
import com.trackademy.adapter.in.rest.dto.CursoResponse;
import com.trackademy.adapter.in.rest.dto.CursoSilaboVersionResponse;
import com.trackademy.application.port.in.CatalogoCursosUseCase;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

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
    @Path("/public/{publicId}")
    public Response obtenerPorPublicId(@PathParam("publicId") UUID publicId) {
        return catalogoCursosUseCase.obtenerPorPublicId(publicId)
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

    @GET
    @Path("/public/{publicId}/detalle")
    public Response obtenerDetallePorPublicId(@PathParam("publicId") UUID publicId) {
        return catalogoCursosUseCase.obtenerDetallePorPublicId(publicId)
                .map(CursoDetalleResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/{codigo}/silabos")
    public List<CursoSilaboVersionResponse> listarSilabosPorCodigo(@PathParam("codigo") String codigo) {
        return catalogoCursosUseCase.listarSilabosPorCodigo(codigo).stream()
                .map(CursoSilaboVersionResponse::from)
                .toList();
    }

    @GET
    @Path("/{codigo}/silabo-vigente")
    public Response obtenerSilaboVigentePorCodigo(@PathParam("codigo") String codigo) {
        return catalogoCursosUseCase.obtenerSilaboVigentePorCodigo(codigo)
                .map(CursoSilaboVersionResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/public/{publicId}/silabos")
    public List<CursoSilaboVersionResponse> listarSilabosPorPublicId(@PathParam("publicId") UUID publicId) {
        return catalogoCursosUseCase.listarSilabosPorPublicId(publicId).stream()
                .map(CursoSilaboVersionResponse::from)
                .toList();
    }

    @GET
    @Path("/public/{publicId}/silabo-vigente")
    public Response obtenerSilaboVigentePorPublicId(@PathParam("publicId") UUID publicId) {
        return catalogoCursosUseCase.obtenerSilaboVigentePorPublicId(publicId)
                .map(CursoSilaboVersionResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/silabos/{silaboId}/pdf")
    @Produces("application/pdf")
    public Response descargarSilaboPdf(@PathParam("silaboId") Long silaboId) {
        return catalogoCursosUseCase.obtenerSilaboDescarga(silaboId)
                .map(download -> {
                    if (!"filesystem".equalsIgnoreCase(download.storageProvider())) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }

                    java.nio.file.Path path = java.nio.file.Path.of(download.storageKey());
                    if (!Files.exists(path) || !Files.isReadable(path)) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }

                    return Response.ok(path.toFile(), download.mimeType())
                            .header("Content-Disposition", "attachment; filename=\"" + download.filename() + "\"")
                            .build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
