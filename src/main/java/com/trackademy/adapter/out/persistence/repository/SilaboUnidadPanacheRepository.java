package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.SilaboUnidadEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SilaboUnidadPanacheRepository implements PanacheRepositoryBase<SilaboUnidadEntity, Long> {

    public List<SilaboUnidadEntity> listarPorSilabo(Long silaboId) {
        return list("silaboId = ?1 order by nro asc", silaboId);
    }
}
