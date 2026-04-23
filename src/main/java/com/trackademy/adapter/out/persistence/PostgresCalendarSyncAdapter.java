package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CalendarSyncAccountEntity;
import com.trackademy.adapter.out.persistence.entity.CalendarSyncEventEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.repository.CalendarSyncAccountPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.CalendarSyncEventPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.application.port.out.CalendarSyncPort;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.calendar.CalendarSyncPlan;
import com.trackademy.domain.model.calendar.CalendarSyncPlanItem;
import com.trackademy.domain.model.calendar.CalendarSyncableEvent;
import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiPeriodoActual;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PostgresCalendarSyncAdapter implements CalendarSyncPort {

    private final MeQueryPort meQueryPort;
    private final UsuarioPanacheRepository usuarioRepository;
    private final CalendarSyncAccountPanacheRepository calendarSyncAccountRepository;
    private final CalendarSyncEventPanacheRepository calendarSyncEventRepository;

    public PostgresCalendarSyncAdapter(
            MeQueryPort meQueryPort,
            UsuarioPanacheRepository usuarioRepository,
            CalendarSyncAccountPanacheRepository calendarSyncAccountRepository,
            CalendarSyncEventPanacheRepository calendarSyncEventRepository
    ) {
        this.meQueryPort = meQueryPort;
        this.usuarioRepository = usuarioRepository;
        this.calendarSyncAccountRepository = calendarSyncAccountRepository;
        this.calendarSyncEventRepository = calendarSyncEventRepository;
    }

    @Override
    public CalendarSyncPlan obtenerPlanGoogle(String email, LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = from;
        LocalDate resolvedTo = to;

        Optional<MiPeriodoActual> periodoOpt = meQueryPort.obtenerPeriodoActual(email);
        if (resolvedFrom == null) {
            resolvedFrom = periodoOpt.map(MiPeriodoActual::periodoFechaInicio).orElse(LocalDate.now());
        }
        if (resolvedTo == null) {
            resolvedTo = periodoOpt.map(MiPeriodoActual::periodoFechaFin).orElse(resolvedFrom.plusDays(30));
        }
        if (resolvedTo.isBefore(resolvedFrom)) {
            LocalDate swap = resolvedFrom;
            resolvedFrom = resolvedTo;
            resolvedTo = swap;
        }

        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(normalize(email));
        Optional<CalendarSyncAccountEntity> accountOpt = usuarioOpt.flatMap(usuario ->
                calendarSyncAccountRepository.buscarPorUsuarioYProvider(usuario.id, "google")
        );

        List<CalendarSyncableEvent> localEvents = meQueryPort.listarCalendario(email, resolvedFrom, resolvedTo).stream()
                .map(this::toSyncableEvent)
                .filter(item -> item.sourceKey() != null && !item.sourceKey().isBlank())
                .sorted(Comparator.comparing(CalendarSyncableEvent::startAt).thenComparing(CalendarSyncableEvent::sourceKey))
                .toList();

        Map<String, CalendarSyncEventEntity> mappingsByKey = new HashMap<>();
        if (accountOpt.isPresent()) {
            LocalDateTime startAt = resolvedFrom.atStartOfDay();
            LocalDateTime endAt = resolvedTo.atTime(LocalTime.MAX);
            for (CalendarSyncEventEntity entity : calendarSyncEventRepository.listarPorCuentaYRango(accountOpt.get().id, startAt, endAt)) {
                mappingsByKey.put(entity.sourceKey, entity);
            }
        }

        List<CalendarSyncPlanItem> items = new ArrayList<>();
        Map<String, CalendarSyncableEvent> localByKey = new HashMap<>();
        for (CalendarSyncableEvent local : localEvents) {
            localByKey.put(local.sourceKey(), local);
            CalendarSyncEventEntity previous = mappingsByKey.get(local.sourceKey());
            if (previous == null) {
                items.add(new CalendarSyncPlanItem(
                        "create",
                        local.sourceKey(),
                        local.sourceType(),
                        local.title(),
                        local.subtitle(),
                        local.startAt(),
                        local.endAt(),
                        local.courseCode(),
                        local.referenceCode(),
                        local.sourceHash(),
                        null,
                        null
                ));
                continue;
            }

            String operation = previous.sourceHash != null && previous.sourceHash.equals(local.sourceHash())
                    ? "noop"
                    : "update";

            items.add(new CalendarSyncPlanItem(
                    operation,
                    local.sourceKey(),
                    local.sourceType(),
                    local.title(),
                    local.subtitle(),
                    local.startAt(),
                    local.endAt(),
                    local.courseCode(),
                    local.referenceCode(),
                    local.sourceHash(),
                    previous.sourceHash,
                    previous.googleEventId
            ));
        }

        for (CalendarSyncEventEntity previous : mappingsByKey.values()) {
            if (localByKey.containsKey(previous.sourceKey)) {
                continue;
            }
            items.add(new CalendarSyncPlanItem(
                    "delete",
                    previous.sourceKey,
                    previous.sourceType,
                    null,
                    null,
                    previous.sourceStartAt,
                    previous.sourceEndAt,
                    null,
                    null,
                    null,
                    previous.sourceHash,
                    previous.googleEventId
            ));
        }

        items.sort(Comparator
                .comparing(CalendarSyncPlanItem::startAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CalendarSyncPlanItem::sourceKey));

        long creates = items.stream().filter(item -> "create".equals(item.operation())).count();
        long updates = items.stream().filter(item -> "update".equals(item.operation())).count();
        long deletes = items.stream().filter(item -> "delete".equals(item.operation())).count();
        long noops = items.stream().filter(item -> "noop".equals(item.operation())).count();

        return new CalendarSyncPlan(
                "google",
                accountOpt.isPresent(),
                accountOpt.map(item -> item.email).orElse(null),
                accountOpt.map(item -> item.calendarId).orElse(null),
                resolvedFrom,
                resolvedTo,
                creates,
                updates,
                deletes,
                noops,
                items
        );
    }

    private CalendarSyncableEvent toSyncableEvent(MiCalendarioEvento event) {
        String sourceType = resolveSourceType(event);
        String sourceKey = buildSourceKey(event, sourceType);
        String title = safe(event.titulo());
        String subtitle = safe(event.subtitulo());
        String courseCode = safe(event.codigoCurso());
        String courseName = safe(event.nombreCurso());
        String referenceCode = safe(event.referenciaCodigo());
        String sourceHash = sha256(String.join("|",
                sourceKey,
                sourceType,
                title,
                subtitle,
                event.inicio() == null ? "" : event.inicio().toString(),
                event.fin() == null ? "" : event.fin().toString(),
                Boolean.toString(event.todoElDia()),
                safe(event.origen()),
                courseCode,
                courseName,
                referenceCode
        ));

        return new CalendarSyncableEvent(
                sourceKey,
                sourceType,
                sourceHash,
                title,
                subtitle,
                event.inicio(),
                event.fin(),
                event.todoElDia(),
                safe(event.origen()),
                courseCode,
                courseName,
                referenceCode
        );
    }

    private String resolveSourceType(MiCalendarioEvento event) {
        if ("horario".equalsIgnoreCase(safe(event.origen()))) {
            return "horario";
        }
        if ("evaluacion".equalsIgnoreCase(safe(event.origen()))) {
            return "evaluacion";
        }
        if ("periodo".equalsIgnoreCase(safe(event.origen()))) {
            return "periodo";
        }
        return safe(event.origen()).isBlank() ? "evento" : safe(event.origen());
    }

    private String buildSourceKey(MiCalendarioEvento event, String sourceType) {
        if ("evaluacion".equals(sourceType) && event.usuarioPeriodoCursoId() != null && event.referenciaCodigo() != null) {
            return "evaluacion:" + event.usuarioPeriodoCursoId() + ":" + normalizeToken(event.referenciaCodigo());
        }

        if ("horario".equals(sourceType) && event.usuarioPeriodoCursoId() != null && event.inicio() != null && event.fin() != null) {
            return "horario:" + event.usuarioPeriodoCursoId()
                    + ":" + event.inicio().toLocalDate()
                    + ":" + event.inicio().toLocalTime()
                    + ":" + event.fin().toLocalTime();
        }

        if ("periodo".equals(sourceType) && event.tipo() != null && event.inicio() != null) {
            return "periodo:" + normalizeToken(event.tipo()) + ":" + event.inicio().toLocalDate();
        }

        return "evento:"
                + normalizeToken(event.origen()) + ":"
                + normalizeToken(event.tipo()) + ":"
                + normalizeToken(event.codigoCurso()) + ":"
                + (event.inicio() == null ? "sin-fecha" : event.inicio()) + ":"
                + normalizeToken(event.titulo());
    }

    private String sha256(String input) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo calcular hash de sync", error);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeToken(String value) {
        return safe(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
