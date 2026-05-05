package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.ApiErrorResponse;
import com.trackademy.adapter.in.rest.dto.CreateFeedbackReportRequest;
import com.trackademy.adapter.in.rest.dto.FeedbackReportResponse;
import com.trackademy.adapter.out.persistence.entity.UserWhatsappLinkEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import com.trackademy.adapter.out.persistence.repository.UserWhatsappLinkPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.application.port.in.FeedbackReportUseCase;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.Locale;

@Path("/api/v1/feedback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackReportResource {

    private static final Logger LOG = Logger.getLogger(FeedbackReportResource.class);

    private final FeedbackReportUseCase feedbackReportUseCase;
    private final AuthUseCase authUseCase;
    private final UsuarioPanacheRepository usuarioRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UserWhatsappLinkPanacheRepository userWhatsappLinkRepository;

    public FeedbackReportResource(
            FeedbackReportUseCase feedbackReportUseCase,
            AuthUseCase authUseCase,
            UsuarioPanacheRepository usuarioRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UserWhatsappLinkPanacheRepository userWhatsappLinkRepository
    ) {
        this.feedbackReportUseCase = feedbackReportUseCase;
        this.authUseCase = authUseCase;
        this.usuarioRepository = usuarioRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.userWhatsappLinkRepository = userWhatsappLinkRepository;
    }

    @POST
    @Path("/reportes")
    @Transactional
    public Response crearReporte(@HeaderParam("Authorization") String authorization, CreateFeedbackReportRequest request) {
        try {
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("El body del reporte es requerido"))
                        .build();
            }

            var principal = authUseCase.authenticate(authorization);
            if (principal.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiErrorResponse.unauthorized("Usuario no autenticado"))
                        .build();
            }

            UsuarioEntity usuario = resolveUsuario(principal.get().email(), principal.get().name());
            if (usuario == null || usuario.id == null) {
                LOG.warn("No se pudo resolver usuarioId para email: " + principal.get().email());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiErrorResponse.unauthorized("Identidad de usuario invalida"))
                        .build();
            }

            CreateFeedbackReportRequest enrichedRequest = enrichRequest(request, usuario, principal.get().name());

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

            if (enrichedRequest.emailReportante() == null || !enrichedRequest.emailReportante().contains("@")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiErrorResponse.validation("No pudimos resolver el email del usuario autenticado"))
                        .build();
            }

            FeedbackReportResponse response = feedbackReportUseCase.crearReporte(usuario.id, enrichedRequest);

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

    private UsuarioEntity resolveUsuario(String email, String name) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null) {
            return null;
        }

        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(normalizedEmail)
                .orElseGet(() -> crearUsuarioMinimo(normalizedEmail, name));

        if (isBlank(usuario.nombre) && !isBlank(name)) {
            usuario.nombre = name.trim();
        }

        return usuario;
    }

    private UsuarioEntity crearUsuarioMinimo(String email, String nombre) {
        if (email == null || email.isBlank()) {
            return null;
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.email = email;
        usuario.nombre = nombre;
        usuarioRepository.persist(usuario);
        usuarioRepository.flush();
        return usuario;
    }

    private CreateFeedbackReportRequest enrichRequest(CreateFeedbackReportRequest request, UsuarioEntity usuario, String principalName) {
        UsuarioPeriodoEntity periodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id).orElse(null);
        UserWhatsappLinkEntity whatsappLink = userWhatsappLinkRepository.buscarPorUsuarioId(usuario.id)
                .filter(link -> Boolean.TRUE.equals(link.verified))
                .orElse(null);

        return new CreateFeedbackReportRequest(
                request.tipo(),
                request.motivo(),
                request.descripcion(),
                firstNonBlank(request.nombreReportante(), usuario.nombrePreferido, usuario.nombre, principalName, usuario.email),
                firstNonBlank(request.emailReportante(), usuario.emailInstitucional, usuario.email),
                firstNonBlank(request.whatsappReportante(), whatsappLink == null ? null : whatsappLink.phoneNumber),
                request.imagenBase64(),
                request.cursoId(),
                request.carreraId() != null ? request.carreraId() : (periodo == null ? null : periodo.carreraId),
                request.ciclo() != null ? request.ciclo() : (periodo == null ? null : periodo.cicloActual),
                request.paginaActual()
        );
    }

    private String normalize(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
