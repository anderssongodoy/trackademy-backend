package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.SilaboAnalysisSnapshotEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class SilaboAnalysisSnapshotPanacheRepository
        implements PanacheRepositoryBase<SilaboAnalysisSnapshotEntity, Long> {

    public Optional<SilaboAnalysisSnapshotEntity> buscarPorHashPdf(String hashPdf) {
        return find("hashPdf = ?1", hashPdf).firstResultOptional();
    }
}
