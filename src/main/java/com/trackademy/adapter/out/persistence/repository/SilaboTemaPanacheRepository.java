package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.SilaboTemaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class SilaboTemaPanacheRepository implements PanacheRepositoryBase<SilaboTemaEntity, Long> {

    public List<SilaboTemaEntity> listarPorUnidades(List<Long> unidadIds) {
        if (unidadIds == null || unidadIds.isEmpty()) {
            return Collections.emptyList();
        }
        return list("silaboUnidadId in ?1 order by silaboUnidadId asc, orden asc", unidadIds);
    }
}
