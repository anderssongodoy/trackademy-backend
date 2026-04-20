package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CursoPanacheRepository implements PanacheRepositoryBase<CursoEntity, Long> {

    public record CursoConCiclo(CursoEntity curso, Integer cicloReferencial) {
    }

    public List<CursoEntity> listarOrdenadosPorCodigo() {
        return list("order by codigo asc");
    }

    public Optional<CursoEntity> buscarPorCodigo(String codigo) {
        Optional<CursoEntity> direct = find("lower(codigo) = ?1", codigo.toLowerCase()).firstResultOptional();
        if (direct.isPresent()) {
            return direct;
        }
        return getEntityManager()
                .createNativeQuery(
                        "select c.* " +
                                "from curso c " +
                                "join curso_codigo_historial h on h.curso_id = c.id " +
                                "where lower(h.codigo) = lower(?1) " +
                                "order by h.es_actual desc, h.last_seen_at desc, c.id desc " +
                                "limit 1",
                        CursoEntity.class
                )
                .setParameter(1, codigo)
                .getResultList()
                .stream()
                .findFirst();
    }

    public Optional<CursoEntity> buscarPorPublicId(UUID publicId) {
        return find("publicId", publicId).firstResultOptional();
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

    public List<CursoConCiclo> listarPorCarreraConCiclo(Long carreraId) {
        if (carreraId == null) {
            return Collections.emptyList();
        }
        List<Object[]> rows = getEntityManager()
                .createNativeQuery(
                        "select c.id, c.public_id, c.universidad_id, c.codigo, c.nombre, c.course_key, c.modalidad, c.creditos, c.horas_semanales, " +
                                "ccc.ciclo_referencial " +
                                "from curso c " +
                                "join curso_carrera cc on cc.curso_id = c.id " +
                                "left join curso_carrera_ciclo ccc on ccc.curso_id = c.id and ccc.carrera_id = cc.carrera_id and ccc.campus_id is null " +
                                "where cc.carrera_id = ?1 " +
                                "order by ccc.ciclo_referencial asc nulls last, c.codigo asc"
                )
                .setParameter(1, carreraId)
                .getResultList();
        return rows.stream().map(this::toCursoConCiclo).toList();
    }

    public List<CursoConCiclo> buscarConCiclo(Long carreraId, String query, Integer limit, Integer offset) {
        if (carreraId == null) {
            return Collections.emptyList();
        }

        int safeLimit = limit == null ? 50 : limit;
        int safeOffset = offset == null ? 0 : offset;

        if (query == null) {
            return getEntityManager()
                    .createNativeQuery(
                            "select c.id, c.public_id, c.universidad_id, c.codigo, c.nombre, c.course_key, c.modalidad, c.creditos, c.horas_semanales, " +
                                    "ccc.ciclo_referencial " +
                                    "from curso c " +
                                    "join curso_carrera cc on cc.curso_id = c.id " +
                                    "left join curso_carrera_ciclo ccc on ccc.curso_id = c.id and ccc.carrera_id = cc.carrera_id and ccc.campus_id is null " +
                                    "where cc.carrera_id = ?1 " +
                                    "order by ccc.ciclo_referencial asc nulls last, c.codigo asc limit ?2 offset ?3"
                    )
                    .setParameter(1, carreraId)
                    .setParameter(2, safeLimit)
                    .setParameter(3, safeOffset)
                    .getResultList()
                    .stream()
                    .map(row -> toCursoConCiclo((Object[]) row))
                    .toList();
        }

        String normalized = "%" + query.toLowerCase() + "%";
        return getEntityManager()
                .createNativeQuery(
                        "select c.id, c.public_id, c.universidad_id, c.codigo, c.nombre, c.course_key, c.modalidad, c.creditos, c.horas_semanales, " +
                                "ccc.ciclo_referencial " +
                                "from curso c " +
                                "join curso_carrera cc on cc.curso_id = c.id " +
                                "left join curso_carrera_ciclo ccc on ccc.curso_id = c.id and ccc.carrera_id = cc.carrera_id and ccc.campus_id is null " +
                                "where cc.carrera_id = ?1 " +
                                "and (" +
                                "unaccent(lower(c.nombre)) like unaccent(?2) " +
                                "or unaccent(lower(c.codigo)) like unaccent(?2) " +
                                "or exists (select 1 from curso_nombre_historial cnh where cnh.curso_id = c.id and unaccent(lower(cnh.nombre)) like unaccent(?2)) " +
                                "or exists (select 1 from curso_codigo_historial cch where cch.curso_id = c.id and unaccent(lower(cch.codigo)) like unaccent(?2))" +
                                ") " +
                                "order by ccc.ciclo_referencial asc nulls last, c.codigo asc limit ?3 offset ?4"
                )
                .setParameter(1, carreraId)
                .setParameter(2, normalized)
                .setParameter(3, safeLimit)
                .setParameter(4, safeOffset)
                .getResultList()
                .stream()
                .map(row -> toCursoConCiclo((Object[]) row))
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
                                    "or exists (select 1 from curso_nombre_historial cnh where cnh.curso_id = curso.id and unaccent(lower(cnh.nombre)) like unaccent(?1)) " +
                                    "or exists (select 1 from curso_codigo_historial cch where cch.curso_id = curso.id and unaccent(lower(cch.codigo)) like unaccent(?1)) " +
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
                                "and (" +
                                "unaccent(lower(c.nombre)) like unaccent(?2) " +
                                "or unaccent(lower(c.codigo)) like unaccent(?2) " +
                                "or exists (select 1 from curso_nombre_historial cnh where cnh.curso_id = c.id and unaccent(lower(cnh.nombre)) like unaccent(?2)) " +
                                "or exists (select 1 from curso_codigo_historial cch where cch.curso_id = c.id and unaccent(lower(cch.codigo)) like unaccent(?2))" +
                                ") " +
                                "order by c.codigo asc limit ?3 offset ?4",
                        CursoEntity.class
                )
                .setParameter(1, carreraId)
                .setParameter(2, normalized)
                .setParameter(3, safeLimit)
                .setParameter(4, safeOffset)
                .getResultList();
    }

    private CursoConCiclo toCursoConCiclo(Object[] row) {
        CursoEntity entity = new CursoEntity();
        entity.id = ((Number) row[0]).longValue();
        entity.publicId = row[1] == null ? null : UUID.fromString(row[1].toString());
        entity.codigo = (String) row[3];
        entity.nombre = (String) row[4];
        entity.modalidad = (String) row[6];
        entity.creditos = row[7] == null ? null : ((Number) row[7]).intValue();
        entity.horasSemanales = row[8] == null ? null : ((Number) row[8]).intValue();
        Integer ciclo = row[9] == null ? null : ((Number) row[9]).intValue();
        return new CursoConCiclo(entity, ciclo);
    }
}
