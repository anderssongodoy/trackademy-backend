package com.trackademy.application.service;

import com.trackademy.application.port.in.MeQueryUseCase;
import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class WhatsappCommandService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("es-PE"));

    private final MeQueryUseCase meQueryUseCase;

    public WhatsappCommandService(MeQueryUseCase meQueryUseCase) {
        this.meQueryUseCase = meQueryUseCase;
    }

    public String resolveCommand(String email, String rawText) {
        String normalized = normalize(rawText);
        return switch (normalized) {
            case "menu" -> "Comandos disponibles: resumen, pendientes, hoy, cursos, ayuda.";
            case "ayuda" -> "Este canal te permite consultar informacion ya registrada en Trackademy. Por ahora no permite registrar ni editar datos.";
            case "resumen" -> buildResumen(email);
            case "pendientes" -> buildPendientes(email);
            case "hoy" -> buildHoy(email);
            case "cursos" -> buildCursos(email);
            default -> isWriteIntent(normalized)
                    ? "Por ahora WhatsApp en Trackademy es solo para consultar informacion. El registro y edicion de datos se realiza desde la web."
                    : "Por ahora este canal es solo de consulta. Escribe: menu, resumen, pendientes, hoy o cursos.";
        };
    }

    private String buildResumen(String email) {
        var dashboard = meQueryUseCase.obtenerDashboard(email).orElse(null);
        if (dashboard == null) {
            return "Todavia no encuentro un resumen listo para tu cuenta. Revisa que tu periodo actual y tus cursos esten configurados en Trackademy.";
        }

        StringBuilder out = new StringBuilder();
        out.append("Resumen Trackademy\n");
        out.append("- Cursos activos: ").append(dashboard.cursosActivos()).append('\n');
        out.append("- Evaluaciones pendientes: ").append(dashboard.evaluacionesPendientes()).append('\n');
        out.append("- Notas registradas: ").append(dashboard.notasRegistradas()).append('\n');

        if (!dashboard.proximasEvaluaciones().isEmpty()) {
            var next = dashboard.proximasEvaluaciones().getFirst();
            out.append("- Siguiente evaluacion: ")
                    .append(next.codigoCurso())
                    .append(" ")
                    .append(next.evaluacionCodigo());
            if (next.fechaEstimada() != null) {
                out.append(" (").append(formatDate(next.fechaEstimada())).append(")");
            }
            out.append('\n');
        }

        if (!dashboard.proximasSesiones().isEmpty()) {
            var next = dashboard.proximasSesiones().getFirst();
            out.append("- Proxima clase: ").append(next.titulo());
        }

        return out.toString().trim();
    }

    private String buildPendientes(String email) {
        List<MiEvaluacionCurso> pendientes = meQueryUseCase.listarMisEvaluaciones(email, null).stream()
                .filter(item -> item.nota() == null && !Boolean.TRUE.equals(item.exonerado()))
                .sorted(Comparator
                        .comparing(MiEvaluacionCurso::fechaEstimada, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MiEvaluacionCurso::codigoCurso, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();

        if (pendientes.isEmpty()) {
            return "No encuentro pendientes registrados en este momento. Si ya configuraste tu ciclo, revisa Trackademy para validar nuevas evaluaciones.";
        }

        StringBuilder out = new StringBuilder("Pendientes\n");
        for (MiEvaluacionCurso item : pendientes) {
            out.append("- ")
                    .append(item.codigoCurso())
                    .append(" ")
                    .append(item.evaluacionCodigo());
            if (item.fechaEstimada() != null) {
                out.append(" · ").append(formatDate(item.fechaEstimada()));
            }
            out.append('\n');
        }
        return out.toString().trim();
    }

    private String buildHoy(String email) {
        LocalDate hoy = LocalDate.now();
        List<MiCalendarioEvento> eventos = meQueryUseCase.listarCalendario(email, hoy, hoy);
        if (eventos.isEmpty()) {
            return "Hoy no veo clases, eventos o pendientes con fecha del dia en Trackademy.";
        }

        StringBuilder out = new StringBuilder("Hoy en Trackademy\n");
        eventos.stream()
                .sorted(Comparator.comparing(MiCalendarioEvento::inicio))
                .limit(5)
                .forEach(item -> out.append("- ")
                        .append(item.titulo())
                        .append(item.todoElDia() ? " · todo el dia" : " · " + item.inicio().toLocalTime())
                        .append('\n'));
        return out.toString().trim();
    }

    private String buildCursos(String email) {
        List<MiCurso> cursos = meQueryUseCase.listarMisCursos(email).stream()
                .filter(item -> Boolean.TRUE.equals(item.activo()))
                .toList();
        if (cursos.isEmpty()) {
            return "Todavia no veo cursos activos en tu periodo actual. Configuralos primero desde Trackademy.";
        }

        StringBuilder out = new StringBuilder("Tus cursos\n");
        cursos.stream()
                .limit(8)
                .forEach(item -> out.append("- ")
                        .append(item.codigo())
                        .append(" · ")
                        .append(item.nombre())
                        .append('\n'));
        return out.toString().trim();
    }

    private String normalize(String rawText) {
        if (rawText == null) {
            return "";
        }
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

    private String formatDate(LocalDate value) {
        return DATE_FORMATTER.format(value);
    }
}
