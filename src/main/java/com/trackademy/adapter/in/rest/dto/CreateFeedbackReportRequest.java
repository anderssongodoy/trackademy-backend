package com.trackademy.adapter.in.rest.dto;

import java.time.LocalDateTime;

public record CreateFeedbackReportRequest(
        String tipo,
        String motivo,
        String descripcion,
        String nombreReportante,
        String emailReportante,
        String whatsappReportante,
        String imagenUrl,
        Long cursoId,
        Long carreraId,
        Integer ciclo,
        String paginaActual
) {
}
