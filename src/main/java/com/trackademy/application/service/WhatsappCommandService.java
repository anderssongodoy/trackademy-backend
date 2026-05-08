package com.trackademy.application.service;

import com.trackademy.application.port.in.MeQueryUseCase;
import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiTarea;
import com.trackademy.domain.model.whatsapp.WspResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class WhatsappCommandService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<DayOfWeek, String> DIA = Map.of(
            DayOfWeek.MONDAY, "Lun",
            DayOfWeek.TUESDAY, "Mar",
            DayOfWeek.WEDNESDAY, "Mie",
            DayOfWeek.THURSDAY, "Jue",
            DayOfWeek.FRIDAY, "Vie",
            DayOfWeek.SATURDAY, "Sab",
            DayOfWeek.SUNDAY, "Dom"
    );

    private final MeQueryUseCase meQueryUseCase;

    public WhatsappCommandService(MeQueryUseCase meQueryUseCase) {
        this.meQueryUseCase = meQueryUseCase;
    }

    public WspResponse resolveCommand(String email, String rawText) {
        String normalized = normalize(rawText);
        return switch (normalized) {
            case "hoy" -> buildHoy(email);
            case "semana" -> buildSemana(email);
            case "examenes" -> buildExamenes(email);
            case "notas" -> buildNotas(email);
            case "tareas" -> buildTareas(email);
            case "cursos" -> buildCursos(email);
            case "menu" -> buildMenu();
            default -> isWriteIntent(normalized)
                    ? new WspResponse.Botones(
                        "WhatsApp en Trackademy es solo para consultar informacion. El registro y edicion se realiza desde la web.",
                        List.of(
                                new WspResponse.Botones.Boton("hoy", "Hoy"),
                                new WspResponse.Botones.Boton("semana", "Esta semana"),
                                new WspResponse.Botones.Boton("menu", "Menu")))
                    : buildMenu();
        };
    }

    private WspResponse buildHoy(String email) {
        LocalDate hoy = LocalDate.now();
        List<MiCalendarioEvento> eventos = meQueryUseCase.listarCalendario(email, hoy, hoy);

        StringBuilder sb = new StringBuilder("*Hoy, ").append(fmtDayDate(hoy)).append("*\n\n");
        if (eventos.isEmpty()) {
            sb.append("Sin clases ni eventos registrados para hoy.");
        } else {
            eventos.stream()
                    .sorted(Comparator.comparing(MiCalendarioEvento::inicio))
                    .limit(7)
                    .forEach(e -> sb.append("• ")
                            .append(e.todoElDia() ? "" : TIME_FMT.format(e.inicio().toLocalTime()) + " · ")
                            .append(e.titulo())
                            .append('\n'));
        }

        return new WspResponse.Botones(sb.toString().trim(), List.of(
                new WspResponse.Botones.Boton("semana", "Esta semana"),
                new WspResponse.Botones.Boton("examenes", "Examenes"),
                new WspResponse.Botones.Boton("tareas", "Mis tareas")
        ));
    }

    private WspResponse buildSemana(String email) {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(6);
        List<MiCalendarioEvento> eventos = meQueryUseCase.listarCalendario(email, hoy, fin);

        StringBuilder sb = new StringBuilder("*Esta semana (")
                .append(DATE_FMT.format(hoy)).append(" – ").append(DATE_FMT.format(fin)).append(")*\n\n");

        if (eventos.isEmpty()) {
            sb.append("Sin eventos registrados para esta semana.");
        } else {
            eventos.stream()
                    .sorted(Comparator.comparing(MiCalendarioEvento::inicio))
                    .limit(9)
                    .forEach(e -> sb.append("• ")
                            .append(fmtDayDate(e.inicio().toLocalDate())).append(" · ")
                            .append(e.todoElDia() ? "" : TIME_FMT.format(e.inicio().toLocalTime()) + " · ")
                            .append(e.titulo())
                            .append('\n'));
        }

        return new WspResponse.Botones(sb.toString().trim(), List.of(
                new WspResponse.Botones.Boton("hoy", "Hoy"),
                new WspResponse.Botones.Boton("examenes", "Examenes"),
                new WspResponse.Botones.Boton("tareas", "Mis tareas")
        ));
    }

    private WspResponse buildExamenes(String email) {
        List<MiEvaluacionCurso> pendientes = meQueryUseCase.listarMisEvaluaciones(email, null).stream()
                .filter(e -> e.nota() == null && !Boolean.TRUE.equals(e.exonerado()))
                .sorted(Comparator
                        .comparing(MiEvaluacionCurso::fechaEstimada, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MiEvaluacionCurso::codigoCurso, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(7)
                .toList();

        if (pendientes.isEmpty()) {
            return new WspResponse.Botones("No hay evaluaciones pendientes registradas.", List.of(
                    new WspResponse.Botones.Boton("notas", "Mis notas"),
                    new WspResponse.Botones.Boton("hoy", "Hoy"),
                    new WspResponse.Botones.Boton("menu", "Menu")
            ));
        }

        StringBuilder sb = new StringBuilder("*Examenes pendientes*\n\n");
        pendientes.forEach(e -> {
            sb.append("• ").append(e.codigoCurso()).append(" · ").append(e.evaluacionCodigo());
            if (e.fechaEstimada() != null) sb.append(" · ").append(DATE_FMT.format(e.fechaEstimada()));
            if (e.porcentaje() != null) sb.append(" (").append(e.porcentaje().stripTrailingZeros().toPlainString()).append("%)");
            sb.append('\n');
        });

        return new WspResponse.Botones(sb.toString().trim(), List.of(
                new WspResponse.Botones.Boton("notas", "Mis notas"),
                new WspResponse.Botones.Boton("hoy", "Hoy"),
                new WspResponse.Botones.Boton("menu", "Menu")
        ));
    }

    private WspResponse buildNotas(String email) {
        List<MiEvaluacionCurso> conNota = meQueryUseCase.listarMisEvaluaciones(email, null).stream()
                .filter(e -> e.nota() != null)
                .sorted(Comparator.comparing(MiEvaluacionCurso::fechaEstimada, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();

        if (conNota.isEmpty()) {
            return new WspResponse.Botones("Aun no hay notas registradas para este ciclo.", List.of(
                    new WspResponse.Botones.Boton("examenes", "Examenes"),
                    new WspResponse.Botones.Boton("cursos", "Mis cursos"),
                    new WspResponse.Botones.Boton("menu", "Menu")
            ));
        }

        StringBuilder sb = new StringBuilder("*Mis notas*\n\n");
        conNota.forEach(e -> sb.append("• ")
                .append(e.codigoCurso()).append(" · ").append(e.evaluacionCodigo())
                .append(": *").append(e.nota().toPlainString()).append("*")
                .append(e.porcentaje() != null ? " (" + e.porcentaje().stripTrailingZeros().toPlainString() + "%)" : "")
                .append('\n'));

        return new WspResponse.Botones(sb.toString().trim(), List.of(
                new WspResponse.Botones.Boton("examenes", "Examenes"),
                new WspResponse.Botones.Boton("cursos", "Mis cursos"),
                new WspResponse.Botones.Boton("menu", "Menu")
        ));
    }

    private WspResponse buildTareas(String email) {
        List<MiTarea> pendientes = meQueryUseCase.listarMisTareas(email).stream()
                .filter(t -> !"COMPLETADO".equals(t.estado()))
                .sorted(Comparator.comparing(MiTarea::fechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(7)
                .toList();

        if (pendientes.isEmpty()) {
            return new WspResponse.Botones("No tienes tareas pendientes. Todo al dia!", List.of(
                    new WspResponse.Botones.Boton("hoy", "Hoy"),
                    new WspResponse.Botones.Boton("semana", "Esta semana"),
                    new WspResponse.Botones.Boton("menu", "Menu")
            ));
        }

        StringBuilder sb = new StringBuilder("*Mis tareas*\n\n");
        pendientes.forEach(t -> {
            sb.append("• ").append(t.titulo());
            if (t.fechaVencimiento() != null) sb.append(" · ").append(DATE_FMT.format(t.fechaVencimiento().toLocalDate()));
            sb.append('\n');
        });

        return new WspResponse.Botones(sb.toString().trim(), List.of(
                new WspResponse.Botones.Boton("hoy", "Hoy"),
                new WspResponse.Botones.Boton("semana", "Esta semana"),
                new WspResponse.Botones.Boton("menu", "Menu")
        ));
    }

    private WspResponse buildCursos(String email) {
        List<MiCurso> cursos = meQueryUseCase.listarMisCursos(email).stream()
                .filter(c -> Boolean.TRUE.equals(c.activo()))
                .toList();

        if (cursos.isEmpty()) {
            return new WspResponse.Botones("Todavia no veo cursos activos. Configuralos desde Trackademy.", List.of(
                    new WspResponse.Botones.Boton("hoy", "Hoy"),
                    new WspResponse.Botones.Boton("examenes", "Examenes"),
                    new WspResponse.Botones.Boton("menu", "Menu")
            ));
        }

        StringBuilder sb = new StringBuilder("*Mis cursos*\n\n");
        cursos.stream().limit(8).forEach(c -> sb.append("• ")
                .append(c.codigo()).append(" · ").append(c.nombre()).append('\n'));

        return new WspResponse.Botones(sb.toString().trim(), List.of(
                new WspResponse.Botones.Boton("hoy", "Hoy"),
                new WspResponse.Botones.Boton("examenes", "Examenes"),
                new WspResponse.Botones.Boton("menu", "Menu")
        ));
    }

    private WspResponse buildMenu() {
        return new WspResponse.Lista(
                "Que quieres consultar?",
                "Ver opciones",
                List.of(
                        new WspResponse.Lista.Seccion("Agenda", List.of(
                                new WspResponse.Lista.Item("hoy", "Hoy", "Clases y eventos de hoy"),
                                new WspResponse.Lista.Item("semana", "Esta semana", "Proximos 7 dias")
                        )),
                        new WspResponse.Lista.Seccion("Evaluaciones", List.of(
                                new WspResponse.Lista.Item("examenes", "Examenes", "Evaluaciones pendientes"),
                                new WspResponse.Lista.Item("notas", "Mis notas", "Notas registradas")
                        )),
                        new WspResponse.Lista.Seccion("General", List.of(
                                new WspResponse.Lista.Item("tareas", "Mis tareas", "Tareas pendientes"),
                                new WspResponse.Lista.Item("cursos", "Mis cursos", "Cursos del ciclo")
                        ))
                )
        );
    }

    private String normalize(String rawText) {
        if (rawText == null) return "";
        return rawText.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isWriteIntent(String normalized) {
        return normalized.startsWith("registr")
                || normalized.startsWith("agrega")
                || normalized.startsWith("anota")
                || normalized.startsWith("pon ")
                || normalized.startsWith("actualiza")
                || normalized.startsWith("edita")
                || normalized.startsWith("cambia");
    }

    private String fmtDayDate(LocalDate d) {
        return DIA.get(d.getDayOfWeek()) + " " + DATE_FMT.format(d);
    }
}
