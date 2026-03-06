package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UsuarioPeriodoPanacheRepository implements PanacheRepositoryBase<UsuarioPeriodoEntity, Long> {

    public Optional<UsuarioPeriodoEntity> buscarPorUsuarioYPeriodo(Long usuarioId, Long periodoId) {
        return find("usuarioId = ?1 and periodoId = ?2", usuarioId, periodoId).firstResultOptional();
    }
}
