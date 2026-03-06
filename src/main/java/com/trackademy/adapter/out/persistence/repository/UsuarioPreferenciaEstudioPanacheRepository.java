package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPreferenciaEstudioEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioPreferenciaEstudioPanacheRepository implements PanacheRepositoryBase<UsuarioPreferenciaEstudioEntity, Long> {

    public void borrarPorUsuarioPeriodo(Long usuarioPeriodoId) {
        delete("usuarioPeriodoId", usuarioPeriodoId);
    }
}
