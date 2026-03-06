package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.PeriodoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PeriodoPanacheRepository implements PanacheRepositoryBase<PeriodoEntity, Long> {

    public List<PeriodoEntity> listarOrdenados() {
        return list("fechaInicio is not null and fechaFin is not null order by fechaInicio desc, id desc");
    }

    public List<PeriodoEntity> listarPorUniversidadOrdenados(Long universidadId) {
        return list("universidadId = ?1 and fechaInicio is not null and fechaFin is not null order by fechaInicio desc, id desc", universidadId);
    }
}
