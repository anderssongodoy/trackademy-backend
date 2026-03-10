package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.OnboardingRequest;
import com.trackademy.adapter.in.rest.dto.OnboardingResponse;
import com.trackademy.application.port.in.OnboardingUseCase;
import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.domain.model.onboarding.OnboardingCommand;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/onboarding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OnboardingResource {

    private final OnboardingUseCase onboardingUseCase;
    private final AuthUseCase authUseCase;

        public OnboardingResource(OnboardingUseCase onboardingUseCase, AuthUseCase authUseCase) {
        this.onboardingUseCase = onboardingUseCase;
                this.authUseCase = authUseCase;
    }

    @POST
    @Path("/basic")
        public Response completarBasico(@HeaderParam("Authorization") String authorization, OnboardingRequest request) {
                var principal = authUseCase.authenticate(authorization);
                if (principal.isEmpty()) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                }

                String email = principal.get().email();

        OnboardingCommand command = new OnboardingCommand(
                                email,
                request.nombre(),
                request.campusId(),
                request.periodoId(),
                request.carreraId(),
                request.cicloActual(),
                request.metaPromedioCiclo(),
                request.horasEstudioSemanaObjetivo(),
                request.cursos() == null ? java.util.List.of() : request.cursos().stream().map(c ->
                        new OnboardingCommand.CursoSeleccionado(
                                c.cursoId(),
                                c.seccion(),
                                c.profesor(),
                                c.modalidad(),
                                c.horarios() == null ? java.util.List.of() : c.horarios().stream().map(h ->
                                        new OnboardingCommand.HorarioCurso(
                                                h.diaSemana(),
                                                h.horaInicio(),
                                                h.horaFin(),
                                                h.tipoSesion(),
                                                h.ubicacion(),
                                                h.urlVirtual()
                                        )
                                ).toList()
                        )
                ).toList(),
                request.franjasPreferidasEstudio() == null ? java.util.List.of() : request.franjasPreferidasEstudio().stream().map(f ->
                        new OnboardingCommand.FranjaEstudioPreferida(
                                f.diaSemana(),
                                f.horaInicio(),
                                f.horaFin(),
                                f.prioridad()
                        )
                ).toList(),
                request.confianzaPorCurso() == null ? java.util.List.of() : request.confianzaPorCurso().stream().map(cf ->
                        new OnboardingCommand.ConfianzaCurso(
                                cf.cursoId(),
                                cf.nivelConfianza(),
                                cf.comentario()
                        )
                ).toList()
        );

        return Response.ok(OnboardingResponse.from(onboardingUseCase.completarOnboardingBasico(command))).build();
    }
}
