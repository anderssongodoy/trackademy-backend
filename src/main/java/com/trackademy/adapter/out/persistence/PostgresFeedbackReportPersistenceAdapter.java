package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.FeedbackReportEntity;
import com.trackademy.adapter.out.persistence.repository.FeedbackReportPanacheRepository;
import com.trackademy.application.port.out.FeedbackReportPersistencePort;
import com.trackademy.domain.model.feedback.FeedbackReport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PostgresFeedbackReportPersistenceAdapter implements FeedbackReportPersistencePort {

    private final FeedbackReportPanacheRepository repository;

    public PostgresFeedbackReportPersistenceAdapter(FeedbackReportPanacheRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FeedbackReport guardar(FeedbackReport report) {
        FeedbackReportEntity entity = mapToEntity(report);
        repository.persistAndFlush(entity);
        return mapToDomain(entity);
    }

    private FeedbackReportEntity mapToEntity(FeedbackReport report) {
        FeedbackReportEntity entity = new FeedbackReportEntity();
        entity.id = report.id();
        entity.usuarioId = report.usuarioId();
        entity.tipo = report.tipo();
        entity.motivo = report.motivo();
        entity.descripcion = report.descripcion();
        entity.nombreReportante = report.nombreReportante();
        entity.emailReportante = report.emailReportante();
        entity.whatsappReportante = report.whatsappReportante();
        entity.imagenUrl = report.imagenUrl();
        entity.cursoId = report.cursoId();
        entity.carreraId = report.carreraId();
        entity.ciclo = report.ciclo();
        entity.paginaActual = report.paginaActual();
        entity.fechaReporte = report.fechaReporte();
        entity.numeroReporte = report.numeroReporte();
        entity.estado = report.estado();
        entity.createdAt = report.createdAt();
        entity.updatedAt = report.updatedAt();
        return entity;
    }

    private FeedbackReport mapToDomain(FeedbackReportEntity entity) {
        return new FeedbackReport(
                entity.id,
                entity.usuarioId,
                entity.tipo,
                entity.motivo,
                entity.descripcion,
                entity.nombreReportante,
                entity.emailReportante,
                entity.whatsappReportante,
                entity.imagenUrl,
                entity.cursoId,
                entity.carreraId,
                entity.ciclo,
                entity.paginaActual,
                entity.fechaReporte,
                entity.numeroReporte,
                entity.estado,
                entity.createdAt,
                entity.updatedAt
        );
    }
}
