package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UsuarioPeriodoCursoPanacheRepository implements PanacheRepositoryBase<UsuarioPeriodoCursoEntity, Long> {

    public Optional<UsuarioPeriodoCursoEntity> buscarPorUsuarioPeriodoYCurso(Long usuarioPeriodoId, Long cursoId) {
        return find("usuarioPeriodoId = ?1 and cursoId = ?2", usuarioPeriodoId, cursoId).firstResultOptional();
    }
}
