package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.CampusCatalogoResponse;
import com.trackademy.adapter.in.rest.dto.CarreraCatalogoResponse;
import com.trackademy.adapter.in.rest.dto.PeriodoCatalogoResponse;
import com.trackademy.adapter.in.rest.dto.PeriodoEventoCatalogoResponse;
import com.trackademy.application.port.in.CatalogoAcademicoUseCase;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/catalog")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoAcademicoResource {

    private final CatalogoAcademicoUseCase useCase;

    public CatalogoAcademicoResource(CatalogoAcademicoUseCase useCase) {
        this.useCase = useCase;
    }

    @GET
    @Path("/campuses")
    public List<CampusCatalogoResponse> listarCampuses(@QueryParam("universidadId") Long universidadId) {
        return useCase.listarCampuses(universidadId).stream().map(CampusCatalogoResponse::from).toList();
    }

    @GET
    @Path("/carreras")
    public List<CarreraCatalogoResponse> listarCarreras(@QueryParam("universidadId") Long universidadId) {
        return useCase.listarCarreras(universidadId).stream().map(CarreraCatalogoResponse::from).toList();
    }

    @GET
    @Path("/periodos")
    public List<PeriodoCatalogoResponse> listarPeriodos(@QueryParam("universidadId") Long universidadId) {
        return useCase.listarPeriodos(universidadId).stream().map(PeriodoCatalogoResponse::from).toList();
    }

    @GET
    @Path("/periodos/{periodoId}/eventos")
    public List<PeriodoEventoCatalogoResponse> listarEventosPeriodo(@PathParam("periodoId") Long periodoId) {
        return useCase.listarEventosPeriodo(periodoId).stream().map(PeriodoEventoCatalogoResponse::from).toList();
    }
}
