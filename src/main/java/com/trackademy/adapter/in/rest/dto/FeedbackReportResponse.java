package com.trackademy.adapter.in.rest.dto;

import java.time.LocalDateTime;

public record FeedbackReportResponse(
        Long id,
        Long usuarioId,
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
        String paginaActual,
        LocalDateTime fechaReporte,
        String numeroReporte,
        String estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
