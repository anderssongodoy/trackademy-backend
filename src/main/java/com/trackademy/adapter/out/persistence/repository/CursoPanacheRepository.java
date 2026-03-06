package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CursoPanacheRepository implements PanacheRepositoryBase<CursoEntity, Long> {

    public List<CursoEntity> listarOrdenadosPorCodigo() {
        return list("order by codigo asc");
    }

    public Optional<CursoEntity> buscarPorCodigo(String codigo) {
        return find("lower(codigo) = ?1", codigo.toLowerCase()).firstResultOptional();
    }
}
