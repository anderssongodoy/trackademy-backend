package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoConfianzaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioPeriodoCursoConfianzaPanacheRepository implements PanacheRepositoryBase<UsuarioPeriodoCursoConfianzaEntity, Long> {

    public void borrarPorUsuarioPeriodoCurso(Long usuarioPeriodoCursoId) {
        delete("usuarioPeriodoCursoId", usuarioPeriodoCursoId);
    }
}
