package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.ActualizarConfiguracionPeriodoRequest;
import com.trackademy.adapter.in.rest.dto.ActualizarPerfilAcademicoRequest;
import com.trackademy.adapter.in.rest.dto.ActualizarPerfilPersonalRequest;
import com.trackademy.adapter.in.rest.dto.ActualizarDatosCursoRequest;
import com.trackademy.adapter.in.rest.dto.ActualizarHorarioCursoRequest;
import com.trackademy.adapter.in.rest.dto.ActualizarHorarioCursoResponse;
import com.trackademy.adapter.in.rest.dto.CalendarSyncExecutionResponse;
import com.trackademy.adapter.in.rest.dto.CalendarSyncPlanResponse;
import com.trackademy.adapter.in.rest.dto.MiCalendarioEventoResponse;
import com.trackademy.adapter.in.rest.dto.MiCalendarSyncAccountResponse;
import com.trackademy.adapter.in.rest.dto.MiCursoResponse;
import com.trackademy.adapter.in.rest.dto.MiDashboardResponse;
import com.trackademy.adapter.in.rest.dto.MiEvaluacionCursoResponse;
import com.trackademy.adapter.in.rest.dto.MiEvaluacionesCursoResumenResponse;
import com.trackademy.adapter.in.rest.dto.MiHorarioCursoResponse;
import com.trackademy.adapter.in.rest.dto.MiPeriodoActualResponse;
import com.trackademy.adapter.in.rest.dto.RegistrarNotaEvaluacionRequest;
import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.application.port.in.CalendarSyncUseCase;
import com.trackademy.application.port.in.MeCommandUseCase;
import com.trackademy.application.port.in.MeQueryUseCase;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    private final MeQueryUseCase meQueryUseCase;
    private final MeCommandUseCase meCommandUseCase;
    private final AuthUseCase authUseCase;
    private final CalendarSyncUseCase calendarSyncUseCase;

    public MeResource(
            MeQueryUseCase meQueryUseCase,
            MeCommandUseCase meCommandUseCase,
            AuthUseCase authUseCase,
            CalendarSyncUseCase calendarSyncUseCase
    ) {
        this.meQueryUseCase = meQueryUseCase;
        this.meCommandUseCase = meCommandUseCase;
        this.authUseCase = authUseCase;
        this.calendarSyncUseCase = calendarSyncUseCase;
    }

    @GET
    @Path("/periodo-actual")
    public Response periodoActual(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
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

    @PUT
    @Path("/periodo-actual")
    public Response actualizarPerfilAcademico(
            @HeaderParam("Authorization") String authorization,
            ActualizarPerfilAcademicoRequest request
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                MiPeriodoActualResponse.from(
                        meCommandUseCase.actualizarPerfilAcademico(principal.get().email(), request.toCommand())
                )
        ).build();
    }

    @PUT
    @Path("/periodo-actual/personal")
    public Response actualizarPerfilPersonal(
            @HeaderParam("Authorization") String authorization,
            ActualizarPerfilPersonalRequest request
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                MiPeriodoActualResponse.from(
                        meCommandUseCase.actualizarPerfilPersonal(principal.get().email(), request.toCommand())
                )
        ).build();
    }

    @PUT
    @Path("/periodo-actual/configuracion")
    public Response actualizarConfiguracionPeriodo(
            @HeaderParam("Authorization") String authorization,
            ActualizarConfiguracionPeriodoRequest request
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                MiPeriodoActualResponse.from(
                        meCommandUseCase.actualizarConfiguracionPeriodo(principal.get().email(), request.toCommand())
                )
        ).build();
    }

    @GET
    @Path("/dashboard")
    public Response dashboard(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return meQueryUseCase.obtenerDashboard(principal.get().email())
                .map(MiDashboardResponse::from)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/cursos")
    public Response misCursos(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();

        List<MiCursoResponse> cursos = meQueryUseCase.listarMisCursos(email).stream()
                .map(MiCursoResponse::from)
                .toList();

        return Response.ok(cursos).build();
    }

    @GET
    @Path("/horarios")
    public Response misHorarios(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();

        List<MiHorarioCursoResponse> horarios = meQueryUseCase.listarMisHorarios(email).stream()
                .map(MiHorarioCursoResponse::from)
                .toList();

        return Response.ok(horarios).build();
    }

    @GET
    @Path("/calendario")
    public Response miCalendario(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<MiCalendarioEventoResponse> eventos = meQueryUseCase.listarCalendario(principal.get().email(), from, to).stream()
                .map(MiCalendarioEventoResponse::from)
                .toList();

        return Response.ok(eventos).build();
    }

    @GET
    @Path("/calendar-sync-accounts")
    public Response misSincronizacionesCalendario(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<MiCalendarSyncAccountResponse> accounts = meQueryUseCase.listarSincronizacionesCalendario(principal.get().email()).stream()
                .map(MiCalendarSyncAccountResponse::from)
                .toList();

        return Response.ok(accounts).build();
    }

    @GET
    @Path("/calendar-sync/google/plan")
    public Response planSincronizacionGoogle(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                CalendarSyncPlanResponse.from(
                        calendarSyncUseCase.obtenerPlanGoogle(principal.get().email(), from, to)
                )
        ).build();
    }

    @POST
    @Path("/calendar-sync/google/sync")
    public Response sincronizarGoogle(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                CalendarSyncExecutionResponse.SyncResponse.from(
                        calendarSyncUseCase.sincronizarGoogle(principal.get().email(), from, to)
                )
        ).build();
    }

    @DELETE
    @Path("/calendar-sync/google")
    public Response desconectarGoogle(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                CalendarSyncExecutionResponse.DisconnectResponse.from(
                        calendarSyncUseCase.desconectarGoogle(principal.get().email())
                )
        ).build();
    }

    @PUT
    @Path("/cursos/{usuarioPeriodoCursoId}")
    public Response actualizarDatosCurso(
            @HeaderParam("Authorization") String authorization,
            @PathParam("usuarioPeriodoCursoId") Long usuarioPeriodoCursoId,
            ActualizarDatosCursoRequest request
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                MiCursoResponse.from(
                        meCommandUseCase.actualizarDatosCurso(
                                principal.get().email(),
                                request.toCommand(usuarioPeriodoCursoId)
                        )
                )
        ).build();
    }

    @PUT
    @Path("/cursos/{usuarioPeriodoCursoId}/horarios")
    public Response actualizarHorario(
            @HeaderParam("Authorization") String authorization,
            @PathParam("usuarioPeriodoCursoId") Long usuarioPeriodoCursoId,
            ActualizarHorarioCursoRequest request
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                ActualizarHorarioCursoResponse.from(
                        meCommandUseCase.actualizarHorarioCurso(principal.get().email(), request.toCommand(usuarioPeriodoCursoId))
                )
        ).build();
    }

    @PUT
    @Path("/cursos/{usuarioPeriodoCursoId}/evaluaciones/{evaluacionCodigo}/nota")
    public Response registrarNotaEvaluacion(
            @HeaderParam("Authorization") String authorization,
            @PathParam("usuarioPeriodoCursoId") Long usuarioPeriodoCursoId,
            @PathParam("evaluacionCodigo") String evaluacionCodigo,
            RegistrarNotaEvaluacionRequest request
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                MiEvaluacionCursoResponse.from(
                        meCommandUseCase.registrarNotaEvaluacion(
                                principal.get().email(),
                                request.toCommand(usuarioPeriodoCursoId, evaluacionCodigo)
                        )
                )
        ).build();
    }

    @GET
    @Path("/evaluaciones/resumen")
    public Response resumenEvaluaciones(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("cursoId") Long cursoId
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();
        return Response.ok(
                MiEvaluacionesCursoResumenResponse.from(
                        meQueryUseCase.obtenerResumenEvaluaciones(email, cursoId)
                )
        ).build();
    }

    @GET
    @Path("/evaluaciones")
    public Response misEvaluaciones(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("cursoId") Long cursoId
    ) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();
        return Response.ok(
                MiEvaluacionesCursoResumenResponse.from(
                        meQueryUseCase.obtenerResumenEvaluaciones(email, cursoId)
                )
        ).build();
    }
}
