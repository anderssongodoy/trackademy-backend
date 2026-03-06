package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.SilaboEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class SilaboPanacheRepository implements PanacheRepositoryBase<SilaboEntity, Long> {

    public Optional<SilaboEntity> buscarVigentePorCursoId(Long cursoId) {
        return find("cursoId = ?1 and vigente = true order by id desc", cursoId)
                .firstResultOptional();
    }
}
