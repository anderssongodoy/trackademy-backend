package com.trackademy.application.port.in;

import com.trackademy.adapter.in.rest.dto.CreateFeedbackReportRequest;
import com.trackademy.adapter.in.rest.dto.FeedbackReportResponse;

public interface FeedbackReportUseCase {
    FeedbackReportResponse crearReporte(Long usuarioId, CreateFeedbackReportRequest request);
}
