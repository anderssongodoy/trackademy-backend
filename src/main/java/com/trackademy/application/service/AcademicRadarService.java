package com.trackademy.application.service;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class AcademicRadarService implements AcademicRadarUseCase {

    private static final BigDecimal DEFAULT_TARGET_GRADE = BigDecimal.valueOf(13);
    private static final int HORIZON_DAYS = 21;

    private final MeQueryUseCase meQueryUseCase;

    public AcademicRadarService(MeQueryUseCase meQueryUseCase) {
        this.meQueryUseCase = meQueryUseCase;
    }

    @Override
    public AcademicRadar obtenerRadar(String email) {
        MiPeriodoActual periodo = meQueryUseCase.obtenerPeriodoActual(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro un periodo academico activo."));

        List<MiCurso> cursos = meQueryUseCase.listarMisCursos(email);
        List<MiEvaluacionCurso> evaluaciones = meQueryUseCase.listarMisEvaluaciones(email, null);
        List<MiTarea> tareas = meQueryUseCase.listarMisTareas(email);

        return buildRadar(periodo, cursos, evaluaciones, tareas, OffsetDateTime.now());
    }

    private AcademicRadar buildRadar(
            MiPeriodoActual periodo,
            List<MiCurso> cursos,
            List<MiEvaluacionCurso> evaluaciones,
            List<MiTarea> tareas,
            OffsetDateTime now
    ) {
        List<MiEvaluacionCurso> activas = evaluaciones.stream()
                .filter(item -> !Boolean.TRUE.equals(item.exonerado()))
                .toList();

        List<RadarCourseRisk> risks = buildCourseRisks(activas, periodo.metaPromedioCiclo());
        Map<Long, RadarCourseRisk> riskByUpc = risks.stream()
                .collect(Collectors.toMap(RadarCourseRisk::usuarioPeriodoCursoId, r -> r, (a, b) -> a));
        List<RadarAction> actions = buildActions(activas, riskByUpc);
        RadarWeeklyLoad weeklyLoad = buildWeeklyLoad(activas, tareas, periodo.horasEstudioSemanaObjetivo());
        RadarAction todayPriority = actions.isEmpty() ? null : actions.getFirst();
        RadarAiInsight insight = buildInsight(todayPriority, actions, weeklyLoad, risks);

        return new AcademicRadar(
                "v3", now, null, null, false, null,
                insight, todayPriority, actions, weeklyLoad, risks
        );
    }

    // ── COURSE RISKS ──────────────────────────────────────────────────────────

    private List<RadarCourseRisk> buildCourseRisks(List<MiEvaluacionCurso> evaluaciones, BigDecimal targetGrade) {
        BigDecimal target = targetGrade == null || targetGrade.compareTo(BigDecimal.ZERO) <= 0
                ? DEFAULT_TARGET_GRADE : targetGrade;
        LocalDate today = LocalDate.now();

        return evaluaciones.stream()
                .collect(Collectors.groupingBy(MiEvaluacionCurso::usuarioPeriodoCursoId))
                .entrySet().stream()
                .map(entry -> {
                    List<MiEvaluacionCurso> items = entry.getValue();
                    MiEvaluacionCurso first = items.getFirst();
                    BigDecimal accumulated = BigDecimal.ZERO;
                    BigDecimal registeredWeight = BigDecimal.ZERO;
                    BigDecimal totalWeight = BigDecimal.ZERO;
                    int overdue = 0;
                    int dueSoon = 0;

                    for (MiEvaluacionCurso item : items) {
                        BigDecimal w = nz(item.porcentaje());
                        totalWeight = totalWeight.add(w);
                        if (item.nota() != null) {
                            registeredWeight = registeredWeight.add(w);
                            accumulated = accumulated.add(
                                    item.nota().multiply(w).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                        } else if (item.fechaEstimada() != null) {
                            long days = ChronoUnit.DAYS.between(today, item.fechaEstimada());
                            if (days < 0) overdue++;
                            else if (days <= 7) dueSoon++;
                        }
                    }

                    BigDecimal pendingWeight = totalWeight.subtract(registeredWeight).max(BigDecimal.ZERO);
                    BigDecimal maxPossible = accumulated.add(
                            pendingWeight.multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                    BigDecimal neededAverage = pendingWeight.compareTo(BigDecimal.ZERO) > 0
                            ? target.subtract(accumulated).multiply(BigDecimal.valueOf(100))
                                    .divide(pendingWeight, 2, RoundingMode.HALF_UP)
                            : null;

                    int score = 20;
                    List<String> reasons = new ArrayList<>();
                    if (maxPossible.compareTo(target) < 0) {
                        score += 60;
                        reasons.add("Incluso con notas perfectas el margen para llegar a la meta es limitado.");
                    }
                    if (neededAverage != null && neededAverage.compareTo(BigDecimal.valueOf(17)) > 0) {
                        score += 36;
                        reasons.add("Necesita promedio alto en lo que queda para alcanzar la meta.");
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
                        reasons.add(dueSoon + " evaluacion" + plural(dueSoon) + " proxima" + plural(dueSoon) + " esta semana.");
                    }
                    if (reasons.isEmpty()) {
                        reasons.add("Situacion estable con la informacion registrada.");
                    }

                    String risk = score >= 74 ? "ALTO" : score >= 48 ? "MEDIO" : "BAJO";
                    return new RadarCourseRisk(
                            first.usuarioPeriodoCursoId(), first.cursoId(),
                            first.codigoCurso(), first.nombreCurso(),
                            scale(accumulated), scale(registeredWeight), scale(pendingWeight),
                            neededAverage == null ? null : scale(neededAverage),
                            risk, Math.min(score, 100), reasons
                    );
                })
                .sorted(Comparator.comparing(RadarCourseRisk::score).reversed()
                        .thenComparing(RadarCourseRisk::nombreCurso, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(6)
                .toList();
    }

    // ── ACTIONS ───────────────────────────────────────────────────────────────

    private List<RadarAction> buildActions(List<MiEvaluacionCurso> evaluaciones, Map<Long, RadarCourseRisk> riskByUpc) {
        LocalDate today = LocalDate.now();

        return evaluaciones.stream()
                .filter(item -> item.nota() == null && item.fechaEstimada() != null)
                .filter(item -> {
                    long days = ChronoUnit.DAYS.between(today, item.fechaEstimada());
                    return days >= -1 && days <= HORIZON_DAYS;
                })
                .map(item -> {
                    long days = ChronoUnit.DAYS.between(today, item.fechaEstimada());
                    BigDecimal weight = nz(item.porcentaje());
                    RadarCourseRisk risk = riskByUpc.get(item.usuarioPeriodoCursoId());

                    int urgency = days <= 0 ? 40 : days == 1 ? 36 : days <= 3 ? 28
                            : days <= 7 ? 18 : days <= 14 ? 9 : 4;
                    int weightScore = Math.min(40, weight.multiply(BigDecimal.valueOf(1.5)).intValue());
                    double complexity = complexityFactor(item.tipo());
                    int riskScore = risk == null ? 0 : switch (risk.risk()) {
                        case "ALTO" -> 20;
                        case "MEDIO" -> 10;
                        default -> 2;
                    };
                    int score = Math.min(100, (int) Math.round((urgency + weightScore) * complexity) + riskScore);
                    int minutes = Math.max(30, Math.min(150, (int) Math.round((40 + weight.intValue() * 2) * complexity)));

                    return new RadarAction(
                            item.usuarioPeriodoCursoId(), item.cursoId(),
                            item.codigoCurso(), item.nombreCurso(),
                            item.evaluacionCodigo(), item.tipo(),
                            item.fechaEstimada(), item.porcentaje(),
                            minutes, score,
                            score >= 70 ? "ALTA" : score >= 45 ? "MEDIA" : "BAJA",
                            buildActionReasons(item, days, weight, risk)
                    );
                })
                .sorted(Comparator.comparing(RadarAction::score).reversed()
                        .thenComparing(RadarAction::fechaEstimada, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();
    }

    private double complexityFactor(String tipo) {
        if (tipo == null) return 1.0;
        String t = tipo.toLowerCase(Locale.ROOT);
        if (t.contains("examen") || t.contains("parcial") || t.contains("final")) return 1.35;
        if (t.contains("trabajo") || t.contains("proyecto") || t.contains("informe") || t.contains("laboratorio")) return 1.15;
        return 1.0;
    }

    private List<String> buildActionReasons(MiEvaluacionCurso item, long days, BigDecimal weight, RadarCourseRisk risk) {
        List<String> reasons = new ArrayList<>();
        if (days <= 0) {
            reasons.add("Vencio ayer o hoy y aun no tiene nota registrada.");
        } else if (days == 1) {
            reasons.add("Vence manana — consolida lo que sabes hoy.");
        } else if (days <= 3) {
            reasons.add("Vence en " + days + " dias — preparacion enfocada ahora.");
        } else if (days <= 7) {
            reasons.add("Vence esta semana, en " + days + " dias.");
        } else {
            reasons.add("Vence en " + days + " dias — buena ventana para preparar sin presion.");
        }
        if (weight.compareTo(BigDecimal.valueOf(20)) >= 0) {
            reasons.add("Pesa " + formatPercent(weight) + " — alto impacto en tu nota final.");
        } else if (weight.compareTo(BigDecimal.ZERO) > 0) {
            reasons.add("Pesa " + formatPercent(weight) + " del total del curso.");
        }
        if (risk != null && "ALTO".equals(risk.risk())) {
            reasons.add("El curso esta en riesgo alto; esta evaluacion puede ser decisiva.");
        } else if (risk != null && "MEDIO".equals(risk.risk())) {
            reasons.add("El curso tiene riesgo medio — buena oportunidad para sumar.");
        }
        return reasons;
    }

    // ── WEEKLY LOAD ───────────────────────────────────────────────────────────

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
                : weekly.size() >= 2 || weight.compareTo(BigDecimal.valueOf(20)) >= 0 || openTasks >= 2
                ? "NORMAL" : "LIGERA";
        int baseMinutes = targetHours == null ? 420 : targetHours * 60;
        int suggested = switch (level) {
            case "PESADA" -> Math.max(baseMinutes, 540);
            case "NORMAL" -> Math.max(baseMinutes, 360);
            default -> Math.max(180, Math.min(baseMinutes, 300));
        };
        String summary = switch (level) {
            case "PESADA" -> "Semana con alta concentracion de evaluaciones. Distribuye tu tiempo con anticipacion.";
            case "NORMAL" -> "Semana manejable. Define bloques de estudio para cada evaluacion pendiente.";
            default -> "Semana ligera. Aprovecha para adelantar material de las proximas semanas.";
        };
        return new RadarWeeklyLoad(from, to, level, weekly.size(), scale(weight), suggested, summary);
    }

    // ── INSIGHT ───────────────────────────────────────────────────────────────

    private RadarAiInsight buildInsight(
            RadarAction todayPriority,
            List<RadarAction> actions,
            RadarWeeklyLoad weeklyLoad,
            List<RadarCourseRisk> risks
    ) {
        if (todayPriority == null || todayPriority.fechaEstimada() == null) {
            String context = switch (weeklyLoad.level()) {
                case "PESADA" -> "Semana cargada. Revisa las fechas de tus evaluaciones y asegurate de tener todo al dia.";
                case "NORMAL" -> "Semana con actividad moderada. Registra las notas pendientes para tener un cuadro claro.";
                default -> "Semana tranquila. Buen momento para revisar material futuro y registrar notas pendientes.";
            };
            return new RadarAiInsight(
                    "Sin evaluaciones urgentes en los proximos " + HORIZON_DAYS + " dias",
                    context,
                    "Revisa tus cursos y asegurate de que las fechas y notas esten actualizadas.",
                    List.of("Verificar fechas en cada curso", "Registrar notas pendientes"),
                    List.of(), "baja", "rules"
            );
        }

        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, todayPriority.fechaEstimada());
        String courseName = displayCourseName(todayPriority.nombreCurso());
        String evalName = displayEvaluationName(todayPriority);
        BigDecimal weight = nz(todayPriority.porcentaje());
        String daysLabel = daysUntil <= 0 ? "hoy" : daysUntil == 1 ? "manana" : "en " + daysUntil + " dias";

        String headline = courseName + " — " + evalName + " vence " + daysLabel;
        String summary = weight.compareTo(BigDecimal.valueOf(20)) >= 0
                ? evalName + " pesa " + formatPercent(weight) + " en " + courseName + " y vence " + daysLabel + ". Es la evaluacion con mayor impacto en tu nota final ahora mismo."
                : "La combinacion de proximidad y peso convierte a " + evalName + " de " + courseName + " en tu prioridad mas rentable esta semana.";
        String todayAction = "Dedica al menos " + todayPriority.suggestedMinutes() + " minutos hoy a " + evalName + " de " + courseName + ".";

        List<String> weeklyPlan = actions.stream()
                .limit(3)
                .map(item -> {
                    long d = ChronoUnit.DAYS.between(today, item.fechaEstimada());
                    String dl = d <= 0 ? "hoy" : d == 1 ? "manana" : "en " + d + " dias";
                    return displayCourseName(item.nombreCurso()) + " — "
                            + displayEvaluationName(item) + " (" + formatPercent(nz(item.porcentaje())) + ") vence " + dl;
                })
                .toList();

        List<String> warnings = risks.stream()
                .filter(item -> "ALTO".equals(item.risk()))
                .limit(2)
                .map(item -> {
                    String needed = item.neededAverage() != null
                            ? "necesita " + item.neededAverage().toPlainString() + " prom. en lo que queda"
                            : "esta en zona critica";
                    return displayCourseName(item.nombreCurso()) + ": " + needed + ".";
                })
                .toList();

        return new RadarAiInsight(headline, summary, todayAction, weeklyPlan, warnings, "alta", "rules");
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

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

    private String displayEvaluationName(RadarAction action) {
        String code = action.evaluacionCodigo();
        String tipo = action.tipo();
        if (code != null && !code.isBlank()) return code;
        if (tipo != null && !tipo.isBlank()) return tipo;
        return "evaluacion pendiente";
    }

    private String displayCourseName(String rawName) {
        if (rawName == null || rawName.isBlank()) return "este curso";
        String trimmed = rawName.trim().replaceAll("\\s+", " ");
        boolean mostlyUpper = trimmed.chars().filter(Character::isLetter).allMatch(ch -> !Character.isLowerCase(ch));
        if (!mostlyUpper) return trimmed;
        String lower = trimmed.toLowerCase(Locale.forLanguageTag("es-PE"));
        StringBuilder out = new StringBuilder(lower.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            if (Character.isLetter(ch) && capitalizeNext) {
                out.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                out.append(ch);
                capitalizeNext = ch == ' ' || ch == '-' || ch == ':';
            }
        }
        return out.toString();
    }
}
