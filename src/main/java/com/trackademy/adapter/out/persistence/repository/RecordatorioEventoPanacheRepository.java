package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.RecordatorioEventoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RecordatorioEventoPanacheRepository implements PanacheRepositoryBase<RecordatorioEventoEntity, Long> {

    public Optional<RecordatorioEventoEntity> buscarPendientePorTarea(Long usuarioTareaId) {
        return find(
                "usuarioTareaId = ?1 and estado = 'pendiente' order by fechaEnvio asc, id asc",
                usuarioTareaId
        ).firstResultOptional();
    }

    public List<RecordatorioEventoEntity> listarPorUsuarioPeriodo(Long usuarioPeriodoId, OffsetDateTime from, OffsetDateTime to) {
        return list(
                "usuarioPeriodoId = ?1 and fechaEnvio >= ?2 and fechaEnvio <= ?3 order by fechaEnvio asc, id asc",
                usuarioPeriodoId,
                from,
                to
        );
    }

    public void cancelarPendientesPorTarea(Long usuarioTareaId) {
        update("estado = 'cancelado' where usuarioTareaId = ?1 and estado = 'pendiente'", usuarioTareaId);
    }
}
