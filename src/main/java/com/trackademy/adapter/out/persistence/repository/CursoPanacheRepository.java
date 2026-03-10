package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
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

    public List<CursoEntity> listarPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return list("id in ?1", ids);
    }

    public List<Long> listarIdsPorCarrera(Long carreraId) {
        if (carreraId == null) {
            return Collections.emptyList();
        }
        List<?> rows = getEntityManager()
                .createNativeQuery("select curso_id from curso_carrera where carrera_id = ?1")
                .setParameter(1, carreraId)
                .getResultList();
        return rows.stream()
                .map(r -> ((Number) r).longValue())
                .toList();
    }
}
