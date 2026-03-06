package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.SilaboEvaluacionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SilaboEvaluacionPanacheRepository implements PanacheRepositoryBase<SilaboEvaluacionEntity, Long> {

    public List<SilaboEvaluacionEntity> listarPorSilabo(Long silaboId) {
        return list("silaboId = ?1 order by semana asc nulls last, codigo asc", silaboId);
    }
}
