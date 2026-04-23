package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CalendarSyncAccountEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CalendarSyncAccountPanacheRepository implements PanacheRepositoryBase<CalendarSyncAccountEntity, Long> {

    public List<CalendarSyncAccountEntity> listarPorUsuario(Long usuarioId) {
        return list("usuarioId = ?1 order by provider asc", usuarioId);
    }

    public Optional<CalendarSyncAccountEntity> buscarPorUsuarioYProvider(Long usuarioId, String provider) {
        return find("usuarioId = ?1 and provider = ?2", usuarioId, provider).firstResultOptional();
    }
}
