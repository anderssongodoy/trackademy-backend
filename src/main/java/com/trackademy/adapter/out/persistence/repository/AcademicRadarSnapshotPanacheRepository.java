package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.AcademicRadarSnapshotEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AcademicRadarSnapshotPanacheRepository implements PanacheRepositoryBase<AcademicRadarSnapshotEntity, Long> {

    public Optional<AcademicRadarSnapshotEntity> buscarPorPeriodoYVersion(Long usuarioPeriodoId, String radarVersion) {
        return find("usuarioPeriodoId = ?1 and radarVersion = ?2", usuarioPeriodoId, radarVersion).firstResultOptional();
    }
}
