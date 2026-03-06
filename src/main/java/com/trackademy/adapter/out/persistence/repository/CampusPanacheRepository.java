package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CampusEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CampusPanacheRepository implements PanacheRepositoryBase<CampusEntity, Long> {

    public List<CampusEntity> listarOrdenados() {
        return list("order by nombre asc");
    }

    public List<CampusEntity> listarPorUniversidadOrdenados(Long universidadId) {
        return list("universidadId = ?1 order by nombre asc", universidadId);
    }
}
