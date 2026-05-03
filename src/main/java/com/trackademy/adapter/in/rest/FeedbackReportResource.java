package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.CreateFeedbackReportRequest;
import com.trackademy.adapter.in.rest.dto.FeedbackReportResponse;
import com.trackademy.application.port.in.FeedbackReportUseCase;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.security.Principal;
import jakarta.ws.rs.core.Context;

@Path("/api/v1/feedback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackReportResource {

    private static final Logger LOG = Logger.getLogger(FeedbackReportResource.class);

    private final FeedbackReportUseCase feedbackReportUseCase;

    public FeedbackReportResource(FeedbackReportUseCase feedbackReportUseCase) {
        this.feedbackReportUseCase = feedbackReportUseCase;
    }

    @POST
    @Path("/reportes")
    @Authenticated
    public Response crearReporte(@Context Principal principal, CreateFeedbackReportRequest request) {
        try {
            LOG.info("Creando nuevo reporte de feedback para usuario: " + principal.getName());

            // Obtener ID del usuario del token
            // En Quarkus con OIDC, el sub claim es el ID del usuario
            Long usuarioId = Long.parseLong(principal.getName());

            // Validar request
            if (request.tipo() == null || request.tipo().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El tipo de reporte es requerido")
                        .build();
            }

            if (request.motivo() == null || request.motivo().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El motivo es requerido")
                        .build();
            }

            if (request.descripcion() == null || request.descripcion().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La descripción es requerida")
                        .build();
            }

            if (request.emailReportante() == null || !request.emailReportante().contains("@")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Email válido requerido")
                        .build();
            }

            FeedbackReportResponse response = feedbackReportUseCase.crearReporte(usuarioId, request);

            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (Exception e) {
            LOG.error("Error al crear reporte de feedback", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al procesar el reporte")
                    .build();
        }
    }
}
