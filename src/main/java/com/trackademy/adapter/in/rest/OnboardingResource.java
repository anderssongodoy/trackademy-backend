package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.OnboardingRequest;
import com.trackademy.adapter.in.rest.dto.OnboardingPdfPreviewResponse;
import com.trackademy.adapter.in.rest.dto.OnboardingResponse;
import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.application.port.in.OnboardingUseCase;
import com.trackademy.application.service.OnboardingPdfPreviewService;
import com.trackademy.domain.model.onboarding.OnboardingCommand;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;

@Path("/api/v1/onboarding")
@Produces(MediaType.APPLICATION_JSON)
public class OnboardingResource {

    private final OnboardingUseCase onboardingUseCase;
    private final AuthUseCase authUseCase;
    private final OnboardingPdfPreviewService onboardingPdfPreviewService;

    public OnboardingResource(
            OnboardingUseCase onboardingUseCase,
            AuthUseCase authUseCase,
            OnboardingPdfPreviewService onboardingPdfPreviewService
    ) {
        this.onboardingUseCase = onboardingUseCase;
        this.authUseCase = authUseCase;
        this.onboardingPdfPreviewService = onboardingPdfPreviewService;
    }

    @POST
    @Path("/basic")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response completarBasico(@HeaderParam("Authorization") String authorization, OnboardingRequest request) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.get().email();

        OnboardingCommand command = new OnboardingCommand(
                email,
                request.nombre(),
                request.nombrePreferido(),
                request.emailInstitucional(),
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
                                f.prioridad(),
                                f.tipo()
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

    @POST
    @Path("/preview-pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response previsualizarPdf(
            @HeaderParam("Authorization") String authorization,
            @RestForm("archivo") FileUpload archivo
    ) throws IOException {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (archivo == null || archivo.uploadedFile() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debes adjuntar un PDF de matricula.")
                    .build();
        }

        try (var stream = java.nio.file.Files.newInputStream(archivo.uploadedFile())) {
            return Response.ok(OnboardingPdfPreviewResponse.from(onboardingPdfPreviewService.previsualizar(stream))).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }
    }
}
