package com.trackademy.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.adapter.out.persistence.entity.AcademicRadarSnapshotEntity;
import com.trackademy.adapter.out.persistence.repository.AcademicRadarSnapshotPanacheRepository;
import com.trackademy.application.port.in.AcademicRadarUseCase;
import com.trackademy.application.port.in.MeQueryUseCase;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.MiTarea;
import com.trackademy.domain.model.radar.AcademicRadar;
import com.trackademy.domain.model.radar.RadarAction;
import com.trackademy.domain.model.radar.RadarAiInsight;
import com.trackademy.domain.model.radar.RadarCourseRisk;
import com.trackademy.domain.model.radar.RadarWeeklyLoad;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class AcademicRadarService implements AcademicRadarUseCase {

    private static final String RADAR_VERSION = "v1";
    private static final BigDecimal DEFAULT_TARGET_GRADE = BigDecimal.valueOf(13);

    private final MeQueryUseCase meQueryUseCase;
    private final AcademicRadarSnapshotPanacheRepository snapshotRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @ConfigProperty(name = "app.ai.enabled", defaultValue = "false")
    boolean aiEnabled;

    @ConfigProperty(name = "app.ai.openai.api-key", defaultValue = "")
    Optional<String> openAiApiKey;

    @ConfigProperty(name = "app.ai.openai.base-url", defaultValue = "https://api.openai.com/v1")
    String openAiBaseUrl;

    @ConfigProperty(name = "app.ai.model", defaultValue = "gpt-5-mini")
    String aiModel;

    @ConfigProperty(name = "app.ai.max-output-tokens", defaultValue = "350")
    int maxOutputTokens;

    @ConfigProperty(name = "app.ai.radar.ttl-hours", defaultValue = "24")
    long radarTtlHours;

    public AcademicRadarService(
            MeQueryUseCase meQueryUseCase,
            AcademicRadarSnapshotPanacheRepository snapshotRepository,
            ObjectMapper objectMapper
    ) {
        this.meQueryUseCase = meQueryUseCase;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    @Override
    @Transactional
    public AcademicRadar obtenerRadar(String email) {
        MiPeriodoActual periodo = meQueryUseCase.obtenerPeriodoActual(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro un periodo academico activo."));

        List<MiCurso> cursos = meQueryUseCase.listarMisCursos(email);
        List<MiEvaluacionCurso> evaluaciones = meQueryUseCase.listarMisEvaluaciones(email, null);
        List<MiTarea> tareas = meQueryUseCase.listarMisTareas(email);
        String inputHash = calcularInputHash(periodo, cursos, evaluaciones, tareas);
        OffsetDateTime now = OffsetDateTime.now();

        Optional<AcademicRadarSnapshotEntity> snapshotOpt = snapshotRepository.buscarPorPeriodoYVersion(
                periodo.usuarioPeriodoId(),
                RADAR_VERSION
        );
        if (snapshotOpt.isPresent()) {
            AcademicRadarSnapshotEntity snapshot = snapshotOpt.get();
            if (inputHash.equals(snapshot.inputHash) && snapshot.validUntil.isAfter(now)) {
                return fromJson(snapshot.payloadJson);
            }
        }

        AcademicRadar radar = buildRadar(periodo, cursos, evaluaciones, tareas, inputHash, now);
        AcademicRadar enriched = maybeGenerateAiInsight(radar, periodo);
        persistSnapshot(snapshotOpt.orElseGet(AcademicRadarSnapshotEntity::new), enriched, periodo);
        return enriched;
    }

    private AcademicRadar buildRadar(
            MiPeriodoActual periodo,
            List<MiCurso> cursos,
            List<MiEvaluacionCurso> evaluaciones,
            List<MiTarea> tareas,
            String inputHash,
            OffsetDateTime now
    ) {
        List<MiEvaluacionCurso> evaluacionesActivas = evaluaciones.stream()
                .filter(item -> !Boolean.TRUE.equals(item.exonerado()))
                .toList();
        List<RadarCourseRisk> risks = buildCourseRisks(evaluacionesActivas, periodo.metaPromedioCiclo());
        Map<Long, RadarCourseRisk> riskByUpc = risks.stream()
                .collect(Collectors.toMap(RadarCourseRisk::usuarioPeriodoCursoId, item -> item, (left, right) -> left));
        List<RadarAction> actions = buildActions(evaluacionesActivas, riskByUpc);
        RadarWeeklyLoad weeklyLoad = buildWeeklyLoad(evaluacionesActivas, tareas, periodo.horasEstudioSemanaObjetivo());
        RadarAction todayPriority = actions.isEmpty() ? null : actions.getFirst();
        RadarAiInsight fallbackInsight = buildFallbackInsight(todayPriority, actions, weeklyLoad, risks);

        return new AcademicRadar(
                RADAR_VERSION,
                now,
                now.plusHours(Math.max(1, radarTtlHours)),
                inputHash,
                false,
                null,
                fallbackInsight,
                todayPriority,
                actions,
                weeklyLoad,
                risks
        );
    }

    private List<RadarCourseRisk> buildCourseRisks(List<MiEvaluacionCurso> evaluaciones, BigDecimal targetGrade) {
        BigDecimal target = targetGrade == null || targetGrade.compareTo(BigDecimal.ZERO) <= 0 ? DEFAULT_TARGET_GRADE : targetGrade;
        LocalDate today = LocalDate.now();

        return evaluaciones.stream()
                .collect(Collectors.groupingBy(MiEvaluacionCurso::usuarioPeriodoCursoId))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<MiEvaluacionCurso> items = entry.getValue();
                    MiEvaluacionCurso first = items.getFirst();
                    BigDecimal accumulated = BigDecimal.ZERO;
                    BigDecimal registeredWeight = BigDecimal.ZERO;
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    int overdue = 0;
                    int dueSoon = 0;

                    for (MiEvaluacionCurso item : items) {
                        BigDecimal weight = nz(item.porcentaje());
                        totalWeight = totalWeight.add(weight);
                        if (item.nota() != null) {
                            registeredWeight = registeredWeight.add(weight);
                            accumulated = accumulated.add(item.nota().multiply(weight).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                        } else if (item.fechaEstimada() != null) {
                            long days = java.time.temporal.ChronoUnit.DAYS.between(today, item.fechaEstimada());
                            if (days < 0) {
                                overdue++;
                            } else if (days <= 7) {
                                dueSoon++;
                            }
                        }
                    }

                    BigDecimal pendingWeight = totalWeight.subtract(registeredWeight).max(BigDecimal.ZERO);
                    BigDecimal maxPossible = accumulated.add(pendingWeight.multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                    BigDecimal neededAverage = pendingWeight.compareTo(BigDecimal.ZERO) > 0
                            ? target.subtract(accumulated).multiply(BigDecimal.valueOf(100)).divide(pendingWeight, 2, RoundingMode.HALF_UP)
                            : null;

                    int score = 20;
                    List<String> reasons = new ArrayList<>();
                    if (maxPossible.compareTo(target) < 0) {
                        score += 60;
                        reasons.add("Incluso con notas altas, el margen para llegar a la meta es limitado.");
                    }
                    if (neededAverage != null && neededAverage.compareTo(BigDecimal.valueOf(17)) > 0) {
                        score += 36;
                        reasons.add("Necesita un promedio alto en lo pendiente.");
                    } else if (neededAverage != null && neededAverage.compareTo(BigDecimal.valueOf(14.5)) > 0) {
                        score += 22;
                        reasons.add("Necesita cuidar las siguientes evaluaciones para sostener la meta.");
                    }
                    if (overdue > 0) {
                        score += 24;
                        reasons.add(overdue + " evaluacion" + plural(overdue) + " vencida" + plural(overdue) + " sin nota registrada.");
                    }
                    if (dueSoon > 0) {
                        score += 12;
                        reasons.add(dueSoon + " evaluacion" + plural(dueSoon) + " cercana" + plural(dueSoon) + " esta semana.");
                    }
                    if (reasons.isEmpty()) {
                        reasons.add("Riesgo bajo con la informacion registrada.");
                    }

                    String risk = score >= 74 ? "ALTO" : score >= 48 ? "MEDIO" : "BAJO";
                    return new RadarCourseRisk(
                            first.usuarioPeriodoCursoId(),
                            first.cursoId(),
                            first.codigoCurso(),
                            first.nombreCurso(),
                            scale(accumulated),
                            scale(registeredWeight),
                            scale(pendingWeight),
                            neededAverage == null ? null : scale(neededAverage),
                            risk,
                            Math.min(score, 100),
                            reasons
                    );
                })
                .sorted(Comparator.comparing(RadarCourseRisk::score).reversed()
                        .thenComparing(RadarCourseRisk::nombreCurso, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(6)
                .toList();
    }

    private List<RadarAction> buildActions(List<MiEvaluacionCurso> evaluaciones, Map<Long, RadarCourseRisk> riskByUpc) {
        LocalDate today = LocalDate.now();
        return evaluaciones.stream()
                .filter(item -> item.nota() == null)
                .map(item -> {
                    int days = item.fechaEstimada() == null
                            ? 21
                            : (int) java.time.temporal.ChronoUnit.DAYS.between(today, item.fechaEstimada());
                    BigDecimal weight = nz(item.porcentaje());
                    RadarCourseRisk risk = riskByUpc.get(item.usuarioPeriodoCursoId());

                    int urgencyScore = days < 0 ? 38 : days == 0 ? 36 : Math.max(0, 34 - (days * 4));
                    int weightScore = Math.min(32, weight.multiply(BigDecimal.valueOf(1.25)).intValue());
                    int riskScore = risk == null ? 0 : switch (risk.risk()) {
                        case "ALTO" -> 24;
                        case "MEDIO" -> 14;
                        default -> 6;
                    };
                    int score = Math.min(100, 10 + urgencyScore + weightScore + riskScore);
                    int minutes = Math.min(150, Math.max(45, 45 + (weight.intValue() * 2) + Math.max(0, urgencyScore)));
                    List<String> reasons = new ArrayList<>();

                    if (item.fechaEstimada() == null) {
                        reasons.add("Aun no tiene fecha precisa registrada.");
                    } else if (days < 0) {
                        reasons.add("La evaluacion ya vencio y sigue sin nota registrada.");
                    } else if (days == 0) {
                        reasons.add("La evaluacion esta programada para hoy.");
                    } else if (days <= 7) {
                        reasons.add("Vence en " + days + " dia" + plural(days) + ".");
                    }
                    if (weight.compareTo(BigDecimal.ZERO) > 0) {
                        reasons.add("Pesa " + formatPercent(weight) + " del curso.");
                    }
                    if (risk != null && !"BAJO".equals(risk.risk())) {
                        reasons.add("El curso figura con riesgo " + risk.risk().toLowerCase(Locale.ROOT) + ".");
                    }
                    if (reasons.isEmpty()) {
                        reasons.add("Es una evaluacion pendiente que conviene cerrar antes de que aumente la carga.");
                    }

                    return new RadarAction(
                            item.usuarioPeriodoCursoId(),
                            item.cursoId(),
                            item.codigoCurso(),
                            item.nombreCurso(),
                            item.evaluacionCodigo(),
                            item.tipo(),
                            item.fechaEstimada(),
                            item.porcentaje(),
                            minutes,
                            score,
                            score >= 75 ? "ALTA" : score >= 50 ? "MEDIA" : "BAJA",
                            reasons
                    );
                })
                .sorted(Comparator.comparing(RadarAction::score).reversed()
                        .thenComparing(RadarAction::fechaEstimada, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();
    }

    private RadarWeeklyLoad buildWeeklyLoad(List<MiEvaluacionCurso> evaluaciones, List<MiTarea> tareas, Integer targetHours) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(7);
        List<MiEvaluacionCurso> weekly = evaluaciones.stream()
                .filter(item -> item.nota() == null && item.fechaEstimada() != null)
                .filter(item -> !item.fechaEstimada().isBefore(from) && !item.fechaEstimada().isAfter(to))
                .toList();
        BigDecimal weight = weekly.stream().map(item -> nz(item.porcentaje())).reduce(BigDecimal.ZERO, BigDecimal::add);
        int openTasks = (int) tareas.stream()
                .filter(item -> item.completedAt() == null && item.fechaVencimiento() != null)
                .filter(item -> {
                    LocalDate due = item.fechaVencimiento().toLocalDate();
                    return !due.isBefore(from) && !due.isAfter(to);
                })
                .count();
        String level = weekly.size() >= 4 || weight.compareTo(BigDecimal.valueOf(45)) >= 0 || openTasks >= 4
                ? "PESADA"
                : weekly.size() >= 2 || weight.compareTo(BigDecimal.valueOf(20)) >= 0 || openTasks >= 2 ? "NORMAL" : "LIGERA";
        int baseMinutes = targetHours == null ? 420 : targetHours * 60;
        int suggested = switch (level) {
            case "PESADA" -> Math.max(baseMinutes, 540);
            case "NORMAL" -> Math.max(baseMinutes, 360);
            default -> Math.max(180, Math.min(baseMinutes, 300));
        };
        String summary = "PESADA".equals(level)
                ? "Semana con alta concentracion de evaluaciones o tareas."
                : "NORMAL".equals(level)
                ? "Semana manejable, pero requiere bloques de estudio claros."
                : "Semana ligera con margen para adelantar pendientes.";
        return new RadarWeeklyLoad(from, to, level, weekly.size(), scale(weight), suggested, summary);
    }

    private RadarAiInsight buildFallbackInsight(
            RadarAction todayPriority,
            List<RadarAction> actions,
            RadarWeeklyLoad weeklyLoad,
            List<RadarCourseRisk> risks
    ) {
        if (todayPriority == null) {
            return new RadarAiInsight(
                    "Sin prioridad critica por ahora",
                    "No encontramos evaluaciones pendientes con suficiente informacion para priorizar. Mantener cursos, notas y fechas actualizadas hara que el asistente mejore.",
                    "Revisa tus cursos y registra cualquier nota pendiente.",
                    List.of("Actualizar notas registradas", "Validar fechas de evaluaciones", "Crear tareas para entregas importantes"),
                    List.of(),
                    "media",
                    "rules"
            );
        }

        List<String> weeklyPlan = actions.stream()
                .limit(3)
                .map(item -> item.codigoCurso() + " - " + item.evaluacionCodigo() + ": " + item.suggestedMinutes() + " min")
                .toList();
        List<String> warnings = risks.stream()
                .filter(item -> !"BAJO".equals(item.risk()))
                .limit(2)
                .map(item -> item.codigoCurso() + " esta en riesgo " + item.risk().toLowerCase(Locale.ROOT) + ".")
                .toList();

        return new RadarAiInsight(
                "Prioriza " + todayPriority.codigoCurso() + " hoy",
                "El asistente detecto que " + todayPriority.evaluacionCodigo() + " combina urgencia, peso academico y riesgo del curso.",
                "Dedica " + todayPriority.suggestedMinutes() + " minutos a " + todayPriority.evaluacionCodigo() + " de " + todayPriority.nombreCurso() + ".",
                weeklyPlan,
                warnings,
                "media",
                "rules"
        );
    }

    private AcademicRadar maybeGenerateAiInsight(AcademicRadar radar, MiPeriodoActual periodo) {
        if (!aiEnabled || openAiApiKey.isEmpty() || openAiApiKey.get().isBlank() || radar.todayPriority() == null) {
            return radar;
        }

        try {
            RadarAiInsight insight = callOpenAi(radar, periodo);
            return new AcademicRadar(
                    radar.version(),
                    radar.generatedAt(),
                    radar.validUntil(),
                    radar.inputHash(),
                    true,
                    aiModel,
                    insight,
                    radar.todayPriority(),
                    radar.topActions(),
                    radar.weeklyLoad(),
                    radar.courseRisks()
            );
        } catch (Exception ignored) {
            return radar;
        }
    }

    private RadarAiInsight callOpenAi(AcademicRadar radar, MiPeriodoActual periodo) throws IOException, InterruptedException {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("metaPromedio", periodo.metaPromedioCiclo());
        inputMap.put("horasEstudioSemanaObjetivo", periodo.horasEstudioSemanaObjetivo());
        inputMap.put("todayPriority", radar.todayPriority());
        inputMap.put("topActions", radar.topActions().stream().limit(3).toList());
        inputMap.put("weeklyLoad", radar.weeklyLoad());
        inputMap.put("courseRisks", radar.courseRisks().stream().limit(4).toList());
        String input = objectMapper.writeValueAsString(inputMap);
        String instructions = """
                Eres el Asistente Academico IA de Trackademy para estudiantes universitarios.
                Responde solo JSON valido, sin markdown.
                Debes ser concreto, accionable y honesto. No inventes cursos, notas ni fechas.
                Usa este schema:
                {
                  "headline": "string corto",
                  "summary": "string de 1 a 2 frases",
                  "todayAction": "string accionable",
                  "weeklyPlan": ["maximo 3 items"],
                  "warnings": ["maximo 2 alertas"],
                  "confidence": "alta|media|baja"
                }
                """;

        JsonNode requestBody = objectMapper.valueToTree(Map.of(
                "model", aiModel,
                "instructions", instructions,
                "input", input,
                "max_output_tokens", maxOutputTokens,
                "store", false
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openAiBaseUrl.replaceAll("/+$", "") + "/responses"))
                .timeout(Duration.ofSeconds(18))
                .header("Authorization", "Bearer " + openAiApiKey.orElseThrow())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI API returned status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String outputText = extractOutputText(root);
        JsonNode insightNode = objectMapper.readTree(cleanJsonOutput(outputText));
        return new RadarAiInsight(
                text(insightNode, "headline", radar.insight().headline()),
                text(insightNode, "summary", radar.insight().summary()),
                text(insightNode, "todayAction", radar.insight().todayAction()),
                stringList(insightNode.get("weeklyPlan"), radar.insight().weeklyPlan()),
                stringList(insightNode.get("warnings"), radar.insight().warnings()),
                text(insightNode, "confidence", "media"),
                "openai"
        );
    }

    private String extractOutputText(JsonNode root) throws IOException {
        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            return outputText.asText();
        }
        JsonNode output = root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content == null || !content.isArray()) {
                    continue;
                }
                for (JsonNode part : content) {
                    JsonNode text = part.get("text");
                    if (text != null && text.isTextual()) {
                        return text.asText();
                    }
                }
            }
        }
        throw new IOException("OpenAI response did not include output text");
    }

    private String cleanJsonOutput(String outputText) {
        String value = outputText == null ? "" : outputText.trim();
        if (value.startsWith("```")) {
            int firstBreak = value.indexOf('\n');
            int lastFence = value.lastIndexOf("```");
            if (firstBreak >= 0 && lastFence > firstBreak) {
                value = value.substring(firstBreak + 1, lastFence).trim();
            }
        }
        return value;
    }

    private void persistSnapshot(AcademicRadarSnapshotEntity entity, AcademicRadar radar, MiPeriodoActual periodo) {
        entity.usuarioId = periodo.usuarioId();
        entity.usuarioPeriodoId = periodo.usuarioPeriodoId();
        entity.inputHash = radar.inputHash();
        entity.radarVersion = radar.version();
        entity.model = radar.model();
        entity.aiGenerated = radar.aiGenerated();
        entity.payloadJson = objectMapper.valueToTree(radar);
        entity.generatedAt = radar.generatedAt();
        entity.validUntil = radar.validUntil();
        if (entity.id == null) {
            snapshotRepository.persist(entity);
        }
    }

    private AcademicRadar fromJson(JsonNode payloadJson) {
        try {
            return objectMapper.treeToValue(payloadJson, AcademicRadar.class);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer el snapshot del radar academico.", e);
        }
    }

    private String calcularInputHash(
            MiPeriodoActual periodo,
            List<MiCurso> cursos,
            List<MiEvaluacionCurso> evaluaciones,
            List<MiTarea> tareas
    ) {
        try {
            Map<String, Object> input = new HashMap<>();
            input.put("date", LocalDate.now().toString());
            input.put("periodo", periodo);
            input.put("cursos", cursos.stream()
                    .sorted(Comparator.comparing(MiCurso::usuarioPeriodoCursoId))
                    .toList());
            input.put("evaluaciones", evaluaciones.stream()
                    .sorted(Comparator.comparing(MiEvaluacionCurso::usuarioPeriodoCursoId)
                            .thenComparing(MiEvaluacionCurso::evaluacionCodigo, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList());
            input.put("tareas", tareas.stream()
                    .map(item -> {
                        Map<String, Object> task = new HashMap<>();
                        task.put("id", item.id());
                        task.put("curso", item.usuarioPeriodoCursoId());
                        task.put("estado", item.estado());
                        task.put("vencimiento", item.fechaVencimiento() == null ? "" : item.fechaVencimiento().toString());
                        task.put("updatedAt", item.updatedAt() == null ? "" : item.updatedAt().toString());
                        return task;
                    })
                    .toList());
            String payload = objectMapper.writeValueAsString(input);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular el hash del radar academico.", e);
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private String formatPercent(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "%";
    }

    private String plural(int value) {
        return value == 1 ? "" : "s";
    }

    private String text(JsonNode node, String field, String fallback) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isTextual() && !value.asText().isBlank() ? value.asText() : fallback;
    }

    private List<String> stringList(JsonNode node, List<String> fallback) {
        if (node == null || !node.isArray()) {
            return fallback;
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual() && !item.asText().isBlank()) {
                values.add(item.asText());
            }
        }
        return values.isEmpty() ? fallback : values;
    }
}
