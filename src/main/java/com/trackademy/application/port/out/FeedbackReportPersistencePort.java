package com.trackademy.application.port.out;

import com.trackademy.domain.model.feedback.FeedbackReport;

public interface FeedbackReportPersistencePort {
    FeedbackReport guardar(FeedbackReport report);
}
