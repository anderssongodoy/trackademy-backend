package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.SilaboPdfAssetEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SilaboPdfAssetPanacheRepository implements PanacheRepositoryBase<SilaboPdfAssetEntity, Long> {
}
