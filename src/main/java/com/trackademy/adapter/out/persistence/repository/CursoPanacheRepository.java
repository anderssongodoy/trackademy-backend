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

    public List<CursoEntity> buscar(Long carreraId, String query, Integer limit, Integer offset) {
        int safeLimit = limit == null ? 50 : limit;
        int safeOffset = offset == null ? 0 : offset;

        if (query == null) {
            if (carreraId == null) {
                return getEntityManager()
                        .createNativeQuery(
                                "select * from curso order by codigo asc limit ?1 offset ?2",
                                CursoEntity.class
                        )
                        .setParameter(1, safeLimit)
                        .setParameter(2, safeOffset)
                        .getResultList();
            }

            return getEntityManager()
                    .createNativeQuery(
                            "select c.* from curso c " +
                                    "join curso_carrera cc on cc.curso_id = c.id " +
                                    "where cc.carrera_id = ?1 " +
                                    "order by c.codigo asc limit ?2 offset ?3",
                            CursoEntity.class
                    )
                    .setParameter(1, carreraId)
                    .setParameter(2, safeLimit)
                    .setParameter(3, safeOffset)
                    .getResultList();
        }

        String normalized = "%" + query.toLowerCase() + "%";

        if (carreraId == null) {
            return getEntityManager()
                    .createNativeQuery(
                            "select * from curso " +
                                    "where unaccent(lower(nombre)) like unaccent(?1) " +
                                    "or unaccent(lower(codigo)) like unaccent(?1) " +
                                    "order by codigo asc limit ?2 offset ?3",
                            CursoEntity.class
                    )
                    .setParameter(1, normalized)
                    .setParameter(2, safeLimit)
                    .setParameter(3, safeOffset)
                    .getResultList();
        }

        return getEntityManager()
                .createNativeQuery(
                        "select c.* from curso c " +
                                "join curso_carrera cc on cc.curso_id = c.id " +
                                "where cc.carrera_id = ?1 " +
                                "and (unaccent(lower(c.nombre)) like unaccent(?2) " +
                                "or unaccent(lower(c.codigo)) like unaccent(?2)) " +
                                "order by c.codigo asc limit ?3 offset ?4",
                        CursoEntity.class
                )
                .setParameter(1, carreraId)
                .setParameter(2, normalized)
                .setParameter(3, safeLimit)
                .setParameter(4, safeOffset)
                .getResultList();
    }
}
