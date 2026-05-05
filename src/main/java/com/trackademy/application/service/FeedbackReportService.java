package com.trackademy.application.service;

import com.trackademy.adapter.in.rest.dto.CreateFeedbackReportRequest;
import com.trackademy.adapter.in.rest.dto.FeedbackReportResponse;
import com.trackademy.application.port.in.FeedbackReportUseCase;
import com.trackademy.application.port.out.FeedbackReportEmailPort;
import com.trackademy.application.port.out.FeedbackReportPersistencePort;
import com.trackademy.application.port.out.ImageUploadPort;
import com.trackademy.domain.model.feedback.FeedbackReport;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class FeedbackReportService implements FeedbackReportUseCase {

    private static final Logger LOG = Logger.getLogger(FeedbackReportService.class);

    private final FeedbackReportPersistencePort persistencePort;
    private final FeedbackReportEmailPort emailPort;
    private final ImageUploadPort imageUploadPort;

    public FeedbackReportService(
            FeedbackReportPersistencePort persistencePort,
            FeedbackReportEmailPort emailPort,
            ImageUploadPort imageUploadPort
    ) {
        this.persistencePort = persistencePort;
        this.emailPort = emailPort;
        this.imageUploadPort = imageUploadPort;
    }

    @Override
    public FeedbackReportResponse crearReporte(Long usuarioId, CreateFeedbackReportRequest request) {
        // Generar número de reporte único
        String numeroReporte = generarNumeroReporte();

        // Procesar imagen si se envió
        String imagenUrl = null;
        if (request.imagenBase64() != null && !request.imagenBase64().isEmpty()) {
            try {
                imagenUrl = imageUploadPort.uploadBase64Image(request.imagenBase64(), numeroReporte + ".jpg");
            } catch (Exception e) {
                LOG.error("Error uploading image, continuing without it", e);
                // Continuar sin imagen
            }
        }

        // Crear dominio model
        FeedbackReport report = new FeedbackReport(
                null, // id será generado por la BD
                usuarioId,
                request.tipo(),
                request.motivo(),
                request.descripcion(),
                request.nombreReportante(),
                request.emailReportante(),
                request.whatsappReportante(),
                imagenUrl,
                request.cursoId(),
                request.carreraId(),
                request.ciclo(),
                request.paginaActual(),
                LocalDateTime.now(),
                numeroReporte,
                "abierto",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Persistir
        FeedbackReport reportGuardado = persistencePort.guardar(report);

        // Enviar correo de notificación
        try {
            emailPort.enviarNotificacionReporte(reportGuardado);
        } catch (Exception e) {
            LOG.error("Error enviando correo de notificación para reporte: " + numeroReporte, e);
            // No fallar la operación si el correo falla
        }

        LOG.info("Nuevo reporte de feedback creado: " + numeroReporte);

        return mapToResponse(reportGuardado);
    }

    private String generarNumeroReporte() {
        // Formato: REP-YYYYMMdd-XXXXXXXXXXXX (ej: REP-20260503-A1B2C3D4E5F6)
        String timestamp = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
        return "REP-" + timestamp + "-" + randomSuffix;
    }

    private FeedbackReportResponse mapToResponse(FeedbackReport report) {
        return new FeedbackReportResponse(
                report.id(),
                report.usuarioId(),
                report.tipo(),
                report.motivo(),
                report.descripcion(),
                report.nombreReportante(),
                report.emailReportante(),
                report.whatsappReportante(),
                report.imagenUrl(),
                report.cursoId(),
                report.carreraId(),
                report.ciclo(),
                report.paginaActual(),
                report.fechaReporte(),
                report.numeroReporte(),
                report.estado(),
                report.createdAt(),
                report.updatedAt()
        );
    }
}
