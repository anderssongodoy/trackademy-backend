package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.FeedbackReportEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FeedbackReportPanacheRepository implements PanacheRepositoryBase<FeedbackReportEntity, Long> {

    public Optional<FeedbackReportEntity> buscarPorNumeroReporte(String numeroReporte) {
        return find("numeroReporte", numeroReporte).firstResultOptional();
    }

    public List<FeedbackReportEntity> listarPorUsuario(Long usuarioId) {
        return find("usuarioId", usuarioId)
                .page(0, 100) // últimos 100
                .list();
    }

    public List<FeedbackReportEntity> listarPendientes() {
        return find("estado", "abierto")
                .page(0, 50)
                .list();
    }
}
