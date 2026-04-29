package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioTareaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioTareaPanacheRepository implements PanacheRepositoryBase<UsuarioTareaEntity, Long> {

    public List<UsuarioTareaEntity> listarPorUsuarioPeriodo(Long usuarioPeriodoId) {
        return list("usuarioPeriodoId = ?1 order by fechaVencimiento asc nulls last, createdAt desc", usuarioPeriodoId);
    }

    public List<UsuarioTareaEntity> listarActivasConFechaPorUsuarioPeriodo(Long usuarioPeriodoId) {
        return list(
                "usuarioPeriodoId = ?1 and fechaVencimiento is not null and estado in ('pendiente', 'en_progreso') order by fechaVencimiento asc, id asc",
                usuarioPeriodoId
        );
    }

    public Optional<UsuarioTareaEntity> buscarPorIdYUsuarioPeriodo(Long id, Long usuarioPeriodoId) {
        return find("id = ?1 and usuarioPeriodoId = ?2", id, usuarioPeriodoId).firstResultOptional();
    }
}
