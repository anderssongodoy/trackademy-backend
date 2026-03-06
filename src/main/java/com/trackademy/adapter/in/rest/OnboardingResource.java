package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.OnboardingRequest;
import com.trackademy.adapter.in.rest.dto.OnboardingResponse;
import com.trackademy.application.port.in.OnboardingUseCase;
import com.trackademy.domain.model.onboarding.OnboardingCommand;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/onboarding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OnboardingResource {

    private final OnboardingUseCase onboardingUseCase;

    public OnboardingResource(OnboardingUseCase onboardingUseCase) {
        this.onboardingUseCase = onboardingUseCase;
    }

    @POST
    @Path("/basic")
    public OnboardingResponse completarBasico(OnboardingRequest request) {
        OnboardingCommand command = new OnboardingCommand(
                request.email(),
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

        return OnboardingResponse.from(onboardingUseCase.completarOnboardingBasico(command));
    }
}
