package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.PeriodoEventoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PeriodoEventoPanacheRepository implements PanacheRepositoryBase<PeriodoEventoEntity, Long> {

    public List<PeriodoEventoEntity> listarPorPeriodo(Long periodoId) {
        return list("periodoId = ?1 order by fechaInicio asc, id asc", periodoId);
    }
}
