package com.trackademy.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.adapter.out.auth.SecureTokenCipher;
import com.trackademy.adapter.out.persistence.entity.CalendarSyncAccountEntity;
import com.trackademy.adapter.out.persistence.entity.CalendarSyncEventEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.repository.CalendarSyncAccountPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.CalendarSyncEventPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.application.port.out.CalendarSyncPort;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.calendar.CalendarDisconnectResult;
import com.trackademy.domain.model.calendar.CalendarSyncExecutionResult;
import com.trackademy.domain.model.calendar.CalendarSyncPlan;
import com.trackademy.domain.model.calendar.CalendarSyncPlanItem;
import com.trackademy.domain.model.calendar.CalendarSyncableEvent;
import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.MiTarea;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class PostgresCalendarSyncAdapter implements CalendarSyncPort {

    private static final Logger LOG = Logger.getLogger(PostgresCalendarSyncAdapter.class);
    private static final int ERROR_MESSAGE_MAX_LENGTH = 255;
    private static final DateTimeFormatter GOOGLE_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final MeQueryPort meQueryPort;
    private final UsuarioPanacheRepository usuarioRepository;
    private final CalendarSyncAccountPanacheRepository calendarSyncAccountRepository;
    private final CalendarSyncEventPanacheRepository calendarSyncEventRepository;
    private final SecureTokenCipher tokenCipher;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleTokenUri;
    private final String defaultTimeZone;

    public PostgresCalendarSyncAdapter(
            MeQueryPort meQueryPort,
            UsuarioPanacheRepository usuarioRepository,
            CalendarSyncAccountPanacheRepository calendarSyncAccountRepository,
            CalendarSyncEventPanacheRepository calendarSyncEventRepository,
            SecureTokenCipher tokenCipher,
            ObjectMapper objectMapper,
            @ConfigProperty(name = "app.auth.google.frontend-client-id") Optional<String> googleClientId,
            @ConfigProperty(name = "app.auth.google.client-secret") Optional<String> googleClientSecret,
            @ConfigProperty(name = "app.auth.google.token-uri", defaultValue = "https://oauth2.googleapis.com/token") String googleTokenUri,
            @ConfigProperty(name = "app.calendar.default-time-zone", defaultValue = "America/Lima") String defaultTimeZone
    ) {
        this.meQueryPort = meQueryPort;
        this.usuarioRepository = usuarioRepository;
        this.calendarSyncAccountRepository = calendarSyncAccountRepository;
        this.calendarSyncEventRepository = calendarSyncEventRepository;
        this.tokenCipher = tokenCipher;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.googleClientId = googleClientId.orElse("");
        this.googleClientSecret = googleClientSecret.orElse("");
        this.googleTokenUri = googleTokenUri;
        this.defaultTimeZone = defaultTimeZone;
    }

    @Override
    public CalendarSyncPlan obtenerPlanGoogle(String email, LocalDate from, LocalDate to) {
        SyncContext context = buildContext(email, from, to);
        return toPlan(context);
    }

    @Override
    @Transactional
    public CalendarSyncExecutionResult sincronizarGoogle(String email, LocalDate from, LocalDate to) {
        SyncContext context = buildContext(email, from, to);
        if (context.account() == null) {
            return new CalendarSyncExecutionResult("google", false, null, null, context.from(), context.to(), 0, 0, 0, 0, 0);
        }

        CalendarSyncAccountEntity account = context.account();
        if (!isAccountConnected(account)) {
            return new CalendarSyncExecutionResult("google", false, account.email, effectiveCalendarId(account), context.from(), context.to(), 0, 0, 0, 0, 0);
        }
        String accessToken = resolveAccessToken(account);

        long created = 0;
        long updated = 0;
        long deleted = 0;
        long unchanged = 0;
        long failed = 0;

        for (CalendarSyncPlanItem item : context.items()) {
            try {
                switch (item.operation()) {
                    case "create" -> {
                        String googleEventId = createGoogleEvent(account, accessToken, item);
                        upsertMapping(account, item, googleEventId);
                        created++;
                    }
                    case "update" -> {
                        if (item.googleEventId() == null || item.googleEventId().isBlank()) {
                            String googleEventId = createGoogleEvent(account, accessToken, item);
                            upsertMapping(account, item, googleEventId);
                            created++;
                        } else {
                            String resultingEventId = updateGoogleEvent(account, accessToken, item);
                            upsertMapping(account, item, resultingEventId);
                            updated++;
                        }
                    }
                    case "delete" -> {
                        deleteGoogleEvent(account, accessToken, item);
                        deleteMapping(account.id, item.sourceKey());
                        deleted++;
                    }
                    default -> {
                        touchMapping(account, item);
                        unchanged++;
                    }
                }
            } catch (RuntimeException error) {
                failed++;
                markMappingError(account, item, error.getMessage());
                LOG.warnf(error, "Fallo sync incremental Google Calendar para sourceKey=%s operation=%s", item.sourceKey(), item.operation());
            }
        }

        account.lastSyncAt = OffsetDateTime.now();
        if (!"active".equalsIgnoreCase(account.estado)) {
            account.estado = "active";
        }

        return new CalendarSyncExecutionResult(
                "google",
                true,
                account.email,
                effectiveCalendarId(account),
                context.from(),
                context.to(),
                created,
                updated,
                deleted,
                unchanged,
                failed
        );
    }

    @Override
    @Transactional
    public CalendarDisconnectResult desconectarGoogle(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(normalize(email));
        Optional<CalendarSyncAccountEntity> accountOpt = usuarioOpt.flatMap(usuario ->
                calendarSyncAccountRepository.buscarPorUsuarioYProvider(usuario.id, "google")
        );

        if (accountOpt.isEmpty()) {
            return new CalendarDisconnectResult("google", true, 0);
        }

        CalendarSyncAccountEntity account = accountOpt.get();
        List<CalendarSyncEventEntity> mappings = calendarSyncEventRepository.listarPorCuenta(account.id);
        long removedMappings = mappings.size();
        for (CalendarSyncEventEntity mapping : mappings) {
            calendarSyncEventRepository.delete(mapping);
        }

        account.accessTokenEncrypted = null;
        account.refreshTokenEncrypted = null;
        account.tokenExpiresAt = null;
        account.calendarId = null;
        account.estado = "revoked";
        account.lastSyncAt = null;

        return new CalendarDisconnectResult("google", true, removedMappings);
    }

    private SyncContext buildContext(String email, LocalDate from, LocalDate to) {
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
        final LocalDate finalResolvedFrom = resolvedFrom;
        final LocalDate finalResolvedTo = resolvedTo;

        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(normalize(email));
        Optional<CalendarSyncAccountEntity> accountOpt = usuarioOpt.flatMap(usuario ->
                calendarSyncAccountRepository.buscarPorUsuarioYProvider(usuario.id, "google")
        );

        List<CalendarSyncableEvent> localEvents = meQueryPort.listarCalendario(email, resolvedFrom, resolvedTo).stream()
                .map(this::toSyncableEvent)
                .filter(item -> item.sourceKey() != null && !item.sourceKey().isBlank())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        localEvents.addAll(
                meQueryPort.listarMisTareas(email).stream()
                        .filter(this::isSyncableTask)
                        .map(this::toSyncableTaskEvent)
                        .filter(item -> !item.startAt().toLocalDate().isBefore(finalResolvedFrom) && !item.startAt().toLocalDate().isAfter(finalResolvedTo))
                        .toList()
        );

        localEvents = localEvents.stream()
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

        List<CalendarSyncPlanItem> items = calculatePlanItems(localEvents, mappingsByKey);
        return new SyncContext(resolvedFrom, resolvedTo, accountOpt.orElse(null), items);
    }

    private CalendarSyncPlan toPlan(SyncContext context) {
        long creates = context.items().stream().filter(item -> "create".equals(item.operation())).count();
        long updates = context.items().stream().filter(item -> "update".equals(item.operation())).count();
        long deletes = context.items().stream().filter(item -> "delete".equals(item.operation())).count();
        long noops = context.items().stream().filter(item -> "noop".equals(item.operation())).count();

        return new CalendarSyncPlan(
                "google",
                context.account() != null && isAccountConnected(context.account()),
                context.account() == null ? null : context.account().email,
                context.account() == null ? null : effectiveCalendarId(context.account()),
                context.from(),
                context.to(),
                creates,
                updates,
                deletes,
                noops,
                context.items()
        );
    }

    private List<CalendarSyncPlanItem> calculatePlanItems(List<CalendarSyncableEvent> localEvents, Map<String, CalendarSyncEventEntity> mappingsByKey) {
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
                        local.reminderMinutesBefore(),
                        local.courseCode(),
                        local.referenceCode(),
                        local.sourceHash(),
                        null,
                        null
                ));
                continue;
            }

            String operation = resolveOperation(previous, local);

            items.add(new CalendarSyncPlanItem(
                    operation,
                    local.sourceKey(),
                    local.sourceType(),
                    local.title(),
                    local.subtitle(),
                    local.startAt(),
                    local.endAt(),
                    local.reminderMinutesBefore(),
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
                    null,
                    previous.sourceHash,
                    previous.googleEventId
            ));
        }

        items.sort(Comparator
                .comparing(CalendarSyncPlanItem::startAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CalendarSyncPlanItem::sourceKey));
        return items;
    }

    private String resolveOperation(CalendarSyncEventEntity previous, CalendarSyncableEvent local) {
        boolean hasRemoteEvent = previous.googleEventId != null && !previous.googleEventId.isBlank();
        boolean isSynced = "synced".equalsIgnoreCase(previous.estado);

        if (!hasRemoteEvent) {
            return "create";
        }

        if (!isSynced) {
            return "update";
        }

        return previous.sourceHash != null && previous.sourceHash.equals(local.sourceHash())
                ? "noop"
                : "update";
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
                null,
                safe(event.origen()),
                courseCode,
                courseName,
                referenceCode
        );
    }

    private boolean isSyncableTask(MiTarea task) {
        return task.fechaVencimiento() != null
                && task.id() != null
                && task.estado() != null
                && !"cancelada".equalsIgnoreCase(task.estado())
                && !"completada".equalsIgnoreCase(task.estado());
    }

    private CalendarSyncableEvent toSyncableTaskEvent(MiTarea task) {
        boolean reminderOnly = "recordatorio".equalsIgnoreCase(task.tipo());
        OffsetDateTime baseDate = reminderOnly
                ? firstNonNull(task.fechaRecordatorio(), task.fechaVencimiento())
                : task.fechaVencimiento();
        if (baseDate == null) {
            throw new IllegalStateException("La tarea sincronizable no tiene fecha base.");
        }
        LocalDate localDate = toDefaultZoneLocalDate(baseDate);

        String sourceKey = "tarea:" + task.id();
        String sourceType = "tarea";
        String title = safe(task.titulo());
        String subtitle = buildTaskSubtitle(task);
        String courseCode = safe(task.codigoCurso());
        String referenceCode = "TAREA-" + task.id();
        Integer reminderMinutesBefore = calculateReminderMinutesBefore(task, reminderOnly);
        String sourceHash = sha256(String.join("|",
                sourceKey,
                sourceType,
                title,
                subtitle,
                baseDate.toString(),
                courseCode,
                safe(task.prioridad()),
                safe(task.tipo()),
                safe(task.estado()),
                reminderMinutesBefore == null ? "" : reminderMinutesBefore.toString()
        ));

        return new CalendarSyncableEvent(
                sourceKey,
                sourceType,
                sourceHash,
                title,
                subtitle,
                localDate.atStartOfDay(),
                localDate.atTime(23, 59),
                true,
                reminderMinutesBefore,
                "tarea",
                courseCode,
                safe(task.nombreCurso()),
                referenceCode
        );
    }

    private String buildTaskSubtitle(MiTarea task) {
        StringJoiner joiner = new StringJoiner(" · ");
        if (task.nombreCurso() != null && !task.nombreCurso().isBlank()) {
            joiner.add(task.nombreCurso());
        }
        if (task.descripcion() != null && !task.descripcion().isBlank()) {
            joiner.add(task.descripcion());
        }
        if (task.prioridad() != null && !task.prioridad().isBlank()) {
            joiner.add("Prioridad " + task.prioridad());
        }
        return joiner.toString();
    }

    private String resolveAccessToken(CalendarSyncAccountEntity account) {
        if (account.accessTokenEncrypted == null || account.accessTokenEncrypted.isBlank()) {
            throw new IllegalStateException("La cuenta Google no tiene access token disponible");
        }

        if (account.tokenExpiresAt != null && account.tokenExpiresAt.isBefore(OffsetDateTime.now().plusMinutes(1))) {
            return refreshAccessToken(account);
        }

        return tokenCipher.decrypt(account.accessTokenEncrypted);
    }

    private String refreshAccessToken(CalendarSyncAccountEntity account) {
        if (account.refreshTokenEncrypted == null || account.refreshTokenEncrypted.isBlank()) {
            throw new IllegalStateException("La cuenta Google no tiene refresh token para renovar acceso");
        }
        if (googleClientId.isBlank() || googleClientSecret.isBlank()) {
            throw new IllegalStateException("Google OAuth no tiene client credentials para refresh");
        }

        try {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("client_id", googleClientId);
            form.put("client_secret", googleClientSecret);
            form.put("refresh_token", tokenCipher.decrypt(account.refreshTokenEncrypted));
            form.put("grant_type", "refresh_token");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(googleTokenUri))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formEncode(form)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                account.estado = "error";
                throw new IllegalStateException("Google rechazo la renovacion del access token");
            }

            JsonNode json = objectMapper.readTree(response.body());
            String accessToken = text(json, "access_token");
            long expiresIn = json.path("expires_in").asLong(0L);
            if (accessToken == null || accessToken.isBlank()) {
                account.estado = "error";
                throw new IllegalStateException("Google no devolvio access token en refresh");
            }

            account.accessTokenEncrypted = tokenCipher.encrypt(accessToken);
            account.tokenExpiresAt = expiresIn > 0 ? OffsetDateTime.now().plusSeconds(expiresIn) : null;
            account.estado = "active";
            return accessToken;
        } catch (Exception error) {
            account.estado = "error";
            throw new IllegalStateException("No se pudo renovar el token de Google Calendar", error);
        }
    }

    private String createGoogleEvent(CalendarSyncAccountEntity account, String accessToken, CalendarSyncPlanItem item) {
        String body = buildGoogleEventBody(item);
        HttpResponse<String> response = sendGoogleRequest(
                account,
                accessToken,
                effectiveCalendarId(account),
                "",
                "POST",
                body
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(buildGoogleApiError("crear", response));
        }

        try {
            JsonNode json = objectMapper.readTree(response.body());
            String eventId = text(json, "id");
            if (eventId == null || eventId.isBlank()) {
                throw new IllegalStateException("Google no devolvio id del evento creado");
            }
            return eventId;
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo leer la respuesta de Google al crear evento", error);
        }
    }

    private String updateGoogleEvent(CalendarSyncAccountEntity account, String accessToken, CalendarSyncPlanItem item) {
        HttpResponse<String> response = sendGoogleRequest(
                account,
                accessToken,
                effectiveCalendarId(account),
                "/" + urlEncode(item.googleEventId()),
                "PATCH",
                buildGoogleEventBody(item)
        );

        if (response.statusCode() == 404) {
            String recreated = createGoogleEvent(account, accessToken, item);
            return recreated;
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(buildGoogleApiError("actualizar", response));
        }

        return item.googleEventId();
    }

    private void deleteGoogleEvent(CalendarSyncAccountEntity account, String accessToken, CalendarSyncPlanItem item) {
        if (item.googleEventId() == null || item.googleEventId().isBlank()) {
            return;
        }

        HttpResponse<String> response = sendGoogleRequest(
                account,
                accessToken,
                effectiveCalendarId(account),
                "/" + urlEncode(item.googleEventId()),
                "DELETE",
                null
        );

        if (response.statusCode() == 404 || response.statusCode() == 410) {
            return;
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Google no pudo borrar el evento: " + response.statusCode());
        }
    }

    private HttpResponse<String> sendGoogleRequest(
            CalendarSyncAccountEntity account,
            String accessToken,
            String calendarId,
            String eventPathSuffix,
            String method,
            String body
    ) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/calendar/v3/calendars/"
                            + urlEncode(calendarId)
                            + "/events"
                            + eventPathSuffix))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json");

            if ("POST".equals(method) || "PATCH".equals(method)) {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401 && account.refreshTokenEncrypted != null && !account.refreshTokenEncrypted.isBlank()) {
                String refreshedToken = refreshAccessToken(account);
                HttpRequest.Builder retryBuilder = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.googleapis.com/calendar/v3/calendars/"
                                + urlEncode(calendarId)
                                + "/events"
                                + eventPathSuffix))
                        .header("Authorization", "Bearer " + refreshedToken)
                        .header("Accept", "application/json");
                if ("POST".equals(method) || "PATCH".equals(method)) {
                    retryBuilder.header("Content-Type", "application/json");
                    retryBuilder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
                } else {
                    retryBuilder.method(method, HttpRequest.BodyPublishers.noBody());
                }
                return httpClient.send(retryBuilder.build(), HttpResponse.BodyHandlers.ofString());
            }
            return response;
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo ejecutar request a Google Calendar", error);
        }
    }

    private String buildGoogleEventBody(CalendarSyncPlanItem item) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("summary", item.title() == null || item.title().isBlank() ? "Trackademy" : item.title());
            payload.put("description", buildDescription(item));
            payload.put("location", null);
            payload.put("visibility", "private");

            Map<String, Object> start = new LinkedHashMap<>();
            Map<String, Object> end = new LinkedHashMap<>();
            if (isAllDay(item)) {
                start.put("date", item.startAt().toLocalDate().toString());
                end.put("date", item.endAt().toLocalDate().plusDays(1).toString());
            } else {
                start.put("dateTime", toGoogleDateTime(item.startAt()));
                start.put("timeZone", defaultTimeZone);
                end.put("dateTime", toGoogleDateTime(item.endAt()));
                end.put("timeZone", defaultTimeZone);
            }
            payload.put("start", start);
            payload.put("end", end);

            Map<String, Object> extended = new LinkedHashMap<>();
            Map<String, Object> privateProps = new LinkedHashMap<>();
            privateProps.put("trackademySourceKey", item.sourceKey());
            privateProps.put("trackademySourceType", item.sourceType());
            if (item.referenceCode() != null && !item.referenceCode().isBlank()) {
                privateProps.put("trackademyReferenceCode", item.referenceCode());
            }
            if (item.courseCode() != null && !item.courseCode().isBlank()) {
                privateProps.put("trackademyCourseCode", item.courseCode());
            }
            extended.put("private", privateProps);
            payload.put("extendedProperties", extended);
            payload.put("reminders", buildGoogleReminders(item));

            return objectMapper.writeValueAsString(payload);
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo serializar evento para Google Calendar", error);
        }
    }

    private String buildDescription(CalendarSyncPlanItem item) {
        List<String> lines = new ArrayList<>();
        if (item.subtitle() != null && !item.subtitle().isBlank()) {
            lines.add(item.subtitle());
        }
        if (item.courseCode() != null && !item.courseCode().isBlank()) {
            lines.add("Curso: " + item.courseCode());
        }
        if (item.referenceCode() != null && !item.referenceCode().isBlank()) {
            lines.add("Referencia: " + item.referenceCode());
        }
        lines.add("Generado por Trackademy.");
        return String.join("\n", lines);
    }

    private boolean isAllDay(CalendarSyncPlanItem item) {
        return item.startAt() != null
                && item.endAt() != null
                && item.startAt().toLocalTime().equals(LocalTime.MIN)
                && item.endAt().toLocalTime().equals(LocalTime.of(23, 59));
    }

    private Map<String, Object> buildGoogleReminders(CalendarSyncPlanItem item) {
        Map<String, Object> reminders = new LinkedHashMap<>();
        reminders.put("useDefault", false);

        if (item.reminderMinutesBefore() != null && item.reminderMinutesBefore() > 0) {
            List<Map<String, Object>> overrides = new ArrayList<>();
            Map<String, Object> popup = new LinkedHashMap<>();
            popup.put("method", "popup");
            popup.put("minutes", item.reminderMinutesBefore());
            overrides.add(popup);
            reminders.put("overrides", overrides);
        }

        return reminders;
    }

    private void upsertMapping(CalendarSyncAccountEntity account, CalendarSyncPlanItem item, String googleEventId) {
        CalendarSyncEventEntity mapping = calendarSyncEventRepository
                .buscarPorCuentaYSourceKey(account.id, item.sourceKey())
                .orElseGet(() -> {
                    CalendarSyncEventEntity created = new CalendarSyncEventEntity();
                    created.calendarSyncAccountId = account.id;
                    created.sourceKey = item.sourceKey();
                    created.sourceType = item.sourceType();
                    created.sourceHash = item.currentHash();
                    created.sourceStartAt = item.startAt();
                    created.sourceEndAt = item.endAt();
                    created.googleCalendarId = effectiveCalendarId(account);
                    created.googleEventId = googleEventId;
                    created.estado = "synced";
                    created.errorMessage = null;
                    created.lastSeenAt = OffsetDateTime.now();
                    created.lastSyncedAt = OffsetDateTime.now();
                    calendarSyncEventRepository.persist(created);
                    return created;
                });

        mapping.sourceType = item.sourceType();
        mapping.sourceHash = item.currentHash();
        mapping.sourceStartAt = item.startAt();
        mapping.sourceEndAt = item.endAt();
        mapping.googleCalendarId = effectiveCalendarId(account);
        mapping.googleEventId = googleEventId;
        mapping.estado = "synced";
        mapping.errorMessage = null;
        mapping.lastSeenAt = OffsetDateTime.now();
        mapping.lastSyncedAt = OffsetDateTime.now();
    }

    private void touchMapping(CalendarSyncAccountEntity account, CalendarSyncPlanItem item) {
        calendarSyncEventRepository.buscarPorCuentaYSourceKey(account.id, item.sourceKey())
                .ifPresent(mapping -> {
                    mapping.lastSeenAt = OffsetDateTime.now();
                    mapping.estado = "synced";
                    mapping.errorMessage = null;
                });
    }

    private void markMappingError(CalendarSyncAccountEntity account, CalendarSyncPlanItem item, String errorMessage) {
        if (account == null) {
            return;
        }
        CalendarSyncEventEntity mapping = calendarSyncEventRepository
                .buscarPorCuentaYSourceKey(account.id, item.sourceKey())
                .orElseGet(() -> {
                    CalendarSyncEventEntity created = new CalendarSyncEventEntity();
                    created.calendarSyncAccountId = account.id;
                    created.sourceKey = item.sourceKey();
                    created.sourceType = item.sourceType();
                    created.sourceHash = item.currentHash() != null ? item.currentHash() : item.previousHash();
                    created.sourceStartAt = item.startAt() != null ? item.startAt() : LocalDateTime.now();
                    created.sourceEndAt = item.endAt() != null ? item.endAt() : created.sourceStartAt;
                    created.googleCalendarId = effectiveCalendarId(account);
                    created.googleEventId = item.googleEventId();
                    created.estado = "error";
                    created.errorMessage = truncate(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
                    created.lastSeenAt = OffsetDateTime.now();
                    calendarSyncEventRepository.persist(created);
                    return created;
                });
        mapping.estado = "error";
        mapping.errorMessage = truncate(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
        mapping.lastSeenAt = OffsetDateTime.now();
    }

    private void deleteMapping(Long accountId, String sourceKey) {
        calendarSyncEventRepository.buscarPorCuentaYSourceKey(accountId, sourceKey)
                .ifPresent(calendarSyncEventRepository::delete);
    }

    private String effectiveCalendarId(CalendarSyncAccountEntity account) {
        return account.calendarId == null || account.calendarId.isBlank() ? "primary" : account.calendarId;
    }

    private boolean isAccountConnected(CalendarSyncAccountEntity account) {
        return account != null
                && !"revoked".equalsIgnoreCase(account.estado)
                && account.accessTokenEncrypted != null
                && !account.accessTokenEncrypted.isBlank();
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

    private String formEncode(Map<String, String> values) {
        StringJoiner joiner = new StringJoiner("&");
        values.forEach((key, value) -> joiner.add(urlEncode(key) + "=" + urlEncode(value)));
        return joiner.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String text(JsonNode json, String field) {
        JsonNode value = json.path(field);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
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

    private OffsetDateTime firstNonNull(OffsetDateTime first, OffsetDateTime second) {
        return first != null ? first : second;
    }

    private Integer calculateReminderMinutesBefore(MiTarea task, boolean reminderOnly) {
        if (reminderOnly || task.fechaRecordatorio() == null || task.fechaVencimiento() == null) {
            return null;
        }

        long minutes = java.time.Duration.between(task.fechaRecordatorio(), task.fechaVencimiento()).toMinutes();
        if (minutes <= 0 || minutes > 40_320) {
            return null;
        }
        return (int) minutes;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String toGoogleDateTime(LocalDateTime value) {
        return value.atZone(ZoneId.of(defaultTimeZone))
                .toOffsetDateTime()
                .format(GOOGLE_DATE_TIME_FORMATTER);
    }

    private LocalDate toDefaultZoneLocalDate(OffsetDateTime value) {
        return value.atZoneSameInstant(ZoneId.of(defaultTimeZone)).toLocalDate();
    }

    private String buildGoogleApiError(String action, HttpResponse<String> response) {
        String body = safe(response.body());
        if (body.isBlank()) {
            return "Google no pudo " + action + " el evento: " + response.statusCode();
        }
        return "Google no pudo " + action + " el evento: " + response.statusCode() + " - " + body;
    }

    private record SyncContext(
            LocalDate from,
            LocalDate to,
            CalendarSyncAccountEntity account,
            List<CalendarSyncPlanItem> items
    ) {
    }
}
