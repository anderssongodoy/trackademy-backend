package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoHorarioEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UsuarioPeriodoCursoHorarioPanacheRepository implements PanacheRepositoryBase<UsuarioPeriodoCursoHorarioEntity, Long> {

    public void borrarPorUsuarioPeriodoCurso(Long usuarioPeriodoCursoId) {
        delete("usuarioPeriodoCursoId", usuarioPeriodoCursoId);
    }

    public List<UsuarioPeriodoCursoHorarioEntity> listarPorUsuarioPeriodoCursos(List<Long> usuarioPeriodoCursoIds) {
        return list("usuarioPeriodoCursoId in ?1", usuarioPeriodoCursoIds);
    }
}
