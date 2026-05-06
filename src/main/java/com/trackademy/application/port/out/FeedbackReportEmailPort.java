package com.trackademy.application.port.out;

import com.trackademy.domain.model.feedback.FeedbackReport;

public interface FeedbackReportEmailPort {
    void enviarNotificacionReporte(FeedbackReport report);
}
