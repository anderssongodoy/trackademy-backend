package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEvaluacionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioPeriodoEvaluacionPanacheRepository implements PanacheRepositoryBase<UsuarioPeriodoEvaluacionEntity, Long> {

    public List<UsuarioPeriodoEvaluacionEntity> listarPorUsuarioPeriodoCursos(List<Long> usuarioPeriodoCursoIds) {
        if (usuarioPeriodoCursoIds == null || usuarioPeriodoCursoIds.isEmpty()) {
            return List.of();
        }
        return list("usuarioPeriodoCursoId in ?1", usuarioPeriodoCursoIds);
    }

    public Optional<UsuarioPeriodoEvaluacionEntity> buscarPorUsuarioPeriodoCursoYCodigo(Long usuarioPeriodoCursoId, String codigo) {
        return find("usuarioPeriodoCursoId = ?1 and codigo = ?2", usuarioPeriodoCursoId, codigo).firstResultOptional();
    }
}
