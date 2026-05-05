package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.ApiErrorResponse;
import com.trackademy.adapter.in.rest.dto.CreateFeedbackReportRequest;
import com.trackademy.adapter.in.rest.dto.FeedbackReportResponse;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.application.port.in.FeedbackReportUseCase;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.security.Principal;
import java.util.Locale;
import jakarta.ws.rs.core.Context;

@Path("/api/v1/feedback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackReportResource {

    private static final Logger LOG = Logger.getLogger(FeedbackReportResource.class);

    private final FeedbackReportUseCase feedbackReportUseCase;
    private final UsuarioPanacheRepository usuarioRepository;

    public FeedbackReportResource(FeedbackReportUseCase feedbackReportUseCase, UsuarioPanacheRepository usuarioRepository) {
        this.feedbackReportUseCase = feedbackReportUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @POST
    @Path("/reportes")
    @Authenticated
    @Transactional
    public Response crearReporte(@Context Principal principal, CreateFeedbackReportRequest request) {
        try {
            if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
                LOG.warn("No se pudo obtener un principal autenticado válido para crear el reporte");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Usuario no autenticado")
                        .build();
            }

            String principalName = principal.getName();
            LOG.info("Creando nuevo reporte de feedback para usuario: " + principalName);

            Long usuarioId = resolveUsuarioId(principalName);
            if (usuarioId == null) {
                LOG.warn("No se pudo resolver usuarioId para principal: " + principalName);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiErrorResponse.unauthorized("Identidad de usuario inválida"))
                        .build();
            }

            // Validar request
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("El body del reporte es requerido"))
                        .build();
            }

            if (request.tipo() == null || request.tipo().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("El tipo de reporte es requerido"))
                        .build();
            }

            if (request.motivo() == null || request.motivo().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("El motivo es requerido"))
                        .build();
            }

            if (request.descripcion() == null || request.descripcion().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("La descripción es requerida"))
                        .build();
            }

            if (request.emailReportante() == null || !request.emailReportante().contains("@")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("Email válido requerido"))
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

    private Long resolveUsuarioId(String principalName) {
        if (principalName == null || principalName.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException ignored) {
            String email = principalName.trim().toLowerCase(Locale.ROOT);
            return usuarioRepository.buscarPorEmail(email)
                    .map(usuario -> usuario.id)
                    .orElseGet(() -> crearUsuarioMínimo(email, principalName));
        }
    }

    private Long crearUsuarioMínimo(String email, String nombre) {
        if (email == null || email.isBlank()) {
            return null;
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.email = email;
        usuario.nombre = nombre;
        usuarioRepository.persist(usuario);
        usuarioRepository.flush();
        return usuario.id;
    }
}
