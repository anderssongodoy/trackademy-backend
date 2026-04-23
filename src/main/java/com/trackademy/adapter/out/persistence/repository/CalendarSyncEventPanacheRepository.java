package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.CalendarSyncEventEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CalendarSyncEventPanacheRepository implements PanacheRepositoryBase<CalendarSyncEventEntity, Long> {

    public List<CalendarSyncEventEntity> listarPorCuentaYRango(Long calendarSyncAccountId, LocalDateTime from, LocalDateTime to) {
        return list(
                "calendarSyncAccountId = ?1 and sourceEndAt >= ?2 and sourceStartAt <= ?3 order by sourceStartAt asc, sourceKey asc",
                calendarSyncAccountId,
                from,
                to
        );
    }
}
