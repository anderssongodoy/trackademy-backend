package com.trackademy.adapter.out.mail;

import com.trackademy.application.port.out.FeedbackReportEmailPort;
import com.trackademy.domain.model.feedback.FeedbackReport;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class QuarkusMailerFeedbackReportAdapter implements FeedbackReportEmailPort {

    private static final Logger LOG = Logger.getLogger(QuarkusMailerFeedbackReportAdapter.class);

    private final Mailer mailer;
    private final String mailFrom;
    private final String emailDestino;

    public QuarkusMailerFeedbackReportAdapter(
            Mailer mailer,
            @ConfigProperty(name = "quarkus.mailer.from") String mailFrom,
            @ConfigProperty(name = "app.feedback.email.destino") String emailDestino
    ) {
        this.mailer = mailer;
        this.mailFrom = mailFrom;
        this.emailDestino = emailDestino;
    }

    @Override
    public void enviarNotificacionReporte(FeedbackReport report) {
        String asunto = "Nuevo Reporte de Feedback - " + report.numeroReporte();
        String cuerpo = construirCuerpoCorreo(report);

        Mail mail = new Mail()
                .setFrom(mailFrom)
                .addTo(emailDestino)
                .setSubject(asunto)
                .setHtml(cuerpo)
                .addReplyTo(report.emailReportante());

        mailer.send(mail);
        LOG.info("Email de notificación enviado para reporte: " + report.numeroReporte());
    }

    private String construirCuerpoCorreo(FeedbackReport report) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #5B21B6; color: white; padding: 20px; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; background-color: #f9f9f9; border: 1px solid #e0e0e0; border-radius: 0 0 5px 5px; }" +
                ".field { margin: 10px 0; }" +
                ".label { font-weight: bold; color: #5B21B6; }" +
                ".badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; margin: 5px 0; }" +
                ".badge-tipo { background-color: #dbeafe; color: #0c4a6e; }" +
                ".badge-estado { background-color: #dcfce7; color: #166534; }" +
                ".footer { margin-top: 20px; font-size: 12px; color: #999; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h2>Nuevo Reporte de Feedback</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='field'>" +
                "<span class='label'>Número de Reporte:</span> " + report.numeroReporte() + " " +
                "<span class='badge badge-estado'>" + report.estado() + "</span>" +
                "</div>" +
                "<div class='field'>" +
                "<span class='label'>Tipo:</span> " +
                "<span class='badge badge-tipo'>" + formatTipo(report.tipo()) + "</span>" +
                "</div>" +
                "<div class='field'>" +
                "<span class='label'>Motivo:</span> " + escapeHtml(report.motivo()) +
                "</div>" +
                "<div class='field'>" +
                "<span class='label'>Descripción:</span><br>" + escapeHtml(report.descripcion()).replace("\n", "<br>") +
                "</div>" +
                (report.imagenUrl() != null ? "<div class='field'><span class='label'>Evidencia:</span> <a href='" + escapeHtml(report.imagenUrl()) + "'>Ver imagen</a></div>" : "") +
                "<div class='field'>" +
                "<span class='label'>Reportante:</span> " + escapeHtml(report.nombreReportante()) +
                "</div>" +
                "<div class='field'>" +
                "<span class='label'>Email:</span> " + escapeHtml(report.emailReportante()) +
                "</div>" +
                (report.whatsappReportante() != null ? "<div class='field'><span class='label'>WhatsApp:</span> " + escapeHtml(report.whatsappReportante()) + "</div>" : "") +
                (report.paginaActual() != null ? "<div class='field'><span class='label'>Página Actual:</span> " + escapeHtml(report.paginaActual()) + "</div>" : "") +
                "<div class='field'>" +
                "<span class='label'>Fecha:</span> " + report.fechaReporte() +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Este es un correo automático del sistema Trackademy. Por favor no responda directamente.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String formatTipo(String tipo) {
        return switch (tipo) {
            case "sugerencia" -> "Sugerencia";
            case "error" -> "Error";
            case "silabo_desactualizado" -> "Sílabo Desactualizado";
            case "curso_faltante" -> "Curso Faltante";
            default -> "Otro";
        };
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
