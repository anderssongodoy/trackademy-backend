package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoHorarioEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioPeriodoCursoHorarioPanacheRepository implements PanacheRepositoryBase<UsuarioPeriodoCursoHorarioEntity, Long> {

    public void borrarPorUsuarioPeriodoCurso(Long usuarioPeriodoCursoId) {
        delete("usuarioPeriodoCursoId", usuarioPeriodoCursoId);
    }
}
