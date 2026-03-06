package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CarreraEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CarreraPanacheRepository implements PanacheRepositoryBase<CarreraEntity, Long> {

    public List<CarreraEntity> listarOrdenadas() {
        return list("order by nombre asc");
    }

    public List<CarreraEntity> listarPorUniversidadOrdenadas(Long universidadId) {
        return list("universidadId = ?1 order by nombre asc", universidadId);
    }
}
