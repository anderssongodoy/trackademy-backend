package com.trackademy.application.service;

import com.trackademy.application.port.in.CatalogoAcademicoUseCase;
import com.trackademy.application.port.in.CatalogoCursosUseCase;
import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.catalogo.CampusCatalogo;
import com.trackademy.domain.model.catalogo.CarreraCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoCatalogo;
import com.trackademy.domain.model.onboarding.OnboardingPdfPreview;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class OnboardingPdfPreviewService {

    private static final Long DEFAULT_UNIVERSITY_ID = 1L;
    private static final Pattern ALUMNO_PATTERN = Pattern.compile("Alumno\\s*:\\s*([A-Za-z0-9]+)\\s*-\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERIODO_PATTERN = Pattern.compile("Periodo\\s*:\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CAMPUS_PATTERN = Pattern.compile("Campus\\s*:\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COURSE_ROW_PATTERN = Pattern.compile("([0-9A-Z]{4,6})\\s*-\\s*(.+)");
    private static final Pattern SECTION_AND_SCHEDULE_PATTERN = Pattern.compile("(\\d{4,5})\\s*-\\s*(.+)");
    private static final Pattern DAY_TIME_PATTERN = Pattern.compile(
            "(lunes|martes|miercoles|miércoles|jueves|viernes|sabado|sábado|domingo)\\s+([0-9]{1,2}:[0-9]{2})\\s*(?:a\\.m|p\\.m|am|pm)?\\s*a\\s+([0-9]{1,2}:[0-9]{2})",
            Pattern.CASE_INSENSITIVE
    );

    private final CatalogoAcademicoUseCase catalogoAcademicoUseCase;
    private final CatalogoCursosUseCase catalogoCursosUseCase;

    public OnboardingPdfPreviewService(
            CatalogoAcademicoUseCase catalogoAcademicoUseCase,
            CatalogoCursosUseCase catalogoCursosUseCase
    ) {
        this.catalogoAcademicoUseCase = catalogoAcademicoUseCase;
        this.catalogoCursosUseCase = catalogoCursosUseCase;
    }

    public OnboardingPdfPreview previsualizar(InputStream pdfStream) throws IOException {
        byte[] pdfBytes = pdfStream.readAllBytes();
        if (pdfBytes.length == 0) {
            throw new IllegalArgumentException("El PDF no tiene contenido.");
        }

        String rawText = extraerTexto(pdfBytes);
        List<String> advertencias = new ArrayList<>();

        if (rawText.isBlank()) {
            rawText = extraerTextoConOcr(pdfBytes, advertencias);
        }

        String normalizedText = normalize(rawText);
        List<String> lines = splitLines(rawText);

        List<CarreraCatalogo> carreras = catalogoAcademicoUseCase.listarCarreras(DEFAULT_UNIVERSITY_ID);
        CarreraCatalogo carrera = resolverCarrera(carreras);
        List<CampusCatalogo> campuses = catalogoAcademicoUseCase.listarCampuses(DEFAULT_UNIVERSITY_ID);
        List<PeriodoCatalogo> periodos = catalogoAcademicoUseCase.listarPeriodos(DEFAULT_UNIVERSITY_ID);
        List<Curso> cursos = carrera == null
                ? catalogoCursosUseCase.listarCursos()
                : catalogoCursosUseCase.listarCursosPorCarrera(carrera.id());

        String codigoAlumno = extractFirstGroup(lines, ALUMNO_PATTERN, 1);
        String nombreCompleto = extractFirstGroup(lines, ALUMNO_PATTERN, 2);
        String emailInstitucional = codigoAlumno != null ? codigoAlumno.toLowerCase(Locale.ROOT) + "@utp.edu.pe" : null;
        String periodoTexto = extractFirstGroup(lines, PERIODO_PATTERN, 1);
        String campusTexto = extractFirstGroup(lines, CAMPUS_PATTERN, 1);

        CampusCatalogo campus = resolverCampus(campuses, normalize(campusTexto));
        PeriodoCatalogo periodo = resolverPeriodo(periodos, periodoTexto, normalizedText);
        List<OnboardingPdfPreview.CursoDetectado> cursosDetectados = resolverCursos(cursos, lines, normalizedText);

        if (codigoAlumno == null || nombreCompleto == null) {
            advertencias.add("No pudimos leer con claridad los datos del alumno.");
        }
        if (periodo == null) {
            advertencias.add("No pudimos identificar el periodo del PDF.");
        }
        if (cursosDetectados.isEmpty()) {
            advertencias.add("No encontramos cursos reconocibles en el PDF.");
        }

        return new OnboardingPdfPreview(
                codigoAlumno,
                nombreCompleto,
                emailInstitucional,
                carrera != null ? carrera.id() : null,
                carrera != null ? carrera.nombre() : null,
                campus != null ? campus.id() : null,
                campus != null ? campus.nombre() : null,
                campusTexto,
                periodo != null ? periodo.id() : null,
                periodo != null ? periodo.etiqueta() : null,
                periodoTexto,
                null,
                cursosDetectados,
                advertencias
        );
    }

    private String extraerTexto(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return Optional.ofNullable(stripper.getText(document)).orElse("").trim();
        }
    }

    private String extraerTextoConOcr(byte[] pdfBytes, List<String> advertencias) {
        Path tempDir = null;
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            tempDir = Files.createTempDirectory("trackademy-pdf-ocr");
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder text = new StringBuilder();

            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                var image = renderer.renderImageWithDPI(pageIndex, 220, ImageType.RGB);
                Path imagePath = tempDir.resolve("page-" + pageIndex + ".png");
                ImageIO.write(image, "png", imagePath.toFile());
                text.append(runTesseract(imagePath)).append('\n');
            }

            String extracted = text.toString().trim();
            if (extracted.isBlank()) {
                advertencias.add("El PDF parece ser una imagen y no obtuvimos texto util con OCR.");
            } else {
                advertencias.add("Usamos OCR para leer este PDF escaneado.");
            }
            return extracted;
        } catch (IOException | InterruptedException exception) {
            advertencias.add("No pudimos hacer OCR del PDF. Instala tesseract-ocr en el servidor para este tipo de archivo.");
            return "";
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String runTesseract(Path imagePath) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "tesseract",
                imagePath.toString(),
                "stdout",
                "-l",
                "spa+eng"
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (InputStream inputStream = process.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Tesseract devolvio codigo " + exitCode);
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }

    private CarreraCatalogo resolverCarrera(List<CarreraCatalogo> carreras) {
        return carreras.stream()
                .filter(item -> normalize(item.nombre()).contains("ingenieria de sistemas"))
                .findFirst()
                .orElse(null);
    }

    private CampusCatalogo resolverCampus(List<CampusCatalogo> campuses, String normalizedCampusText) {
        if (normalizedCampusText == null || normalizedCampusText.isBlank()) {
            return null;
        }
        return campuses.stream()
                .map(campus -> new ScoredCampus(campus, scoreContains(normalizedCampusText, normalize(campus.nombre()))))
                .filter(item -> item.score() > 0)
                .max(Comparator.comparingInt(ScoredCampus::score))
                .map(ScoredCampus::campus)
                .orElse(null);
    }

    private PeriodoCatalogo resolverPeriodo(List<PeriodoCatalogo> periodos, String periodoTexto, String normalizedText) {
        if (periodoTexto != null && !periodoTexto.isBlank()) {
            String normalizedPeriodoText = normalize(periodoTexto);
            String year = extractYear(normalizedPeriodoText);
            String month = extractMonth(normalizedPeriodoText);

            Optional<PeriodoCatalogo> byLine = periodos.stream()
                    .filter(periodo -> {
                        String etiqueta = normalize(periodo.etiqueta());
                        boolean yearMatches = year == null || etiqueta.contains(year);
                        boolean monthMatches = month == null || etiqueta.contains(month);
                        return yearMatches && monthMatches;
                    })
                    .findFirst();
            if (byLine.isPresent()) {
                return byLine.get();
            }
        }

        return periodos.stream()
                .map(periodo -> new ScoredPeriodo(periodo, scoreContains(normalizedText, normalizePeriodo(periodo.etiqueta()))))
                .filter(item -> item.score() > 0)
                .max(Comparator.comparingInt(ScoredPeriodo::score))
                .map(ScoredPeriodo::periodo)
                .orElseGet(() -> periodos.stream()
                        .filter(periodo -> "activo".equalsIgnoreCase(periodo.estado()))
                        .findFirst()
                        .orElse(null));
    }

    private List<OnboardingPdfPreview.CursoDetectado> resolverCursos(List<Curso> cursos, List<String> lines, String normalizedText) {
        List<ParsedCourseBlock> parsedBlocks = parseCourseBlocks(lines);
        LinkedHashSet<Long> detectedIds = new LinkedHashSet<>();
        List<OnboardingPdfPreview.CursoDetectado> detected = new ArrayList<>();

        for (ParsedCourseBlock block : parsedBlocks) {
            Curso matched = matchCourse(cursos, block, normalizedText);
            if (matched == null || !detectedIds.add(matched.id())) {
                continue;
            }

            detected.add(new OnboardingPdfPreview.CursoDetectado(
                    matched.id(),
                    matched.codigo(),
                    matched.nombre(),
                    block.profesor(),
                    block.seccion(),
                    block.modalidad() != null ? block.modalidad() : matched.modalidad(),
                    block.horarios()
            ));
        }

        return detected;
    }

    private List<ParsedCourseBlock> parseCourseBlocks(List<String> lines) {
        List<ParsedCourseBlock> blocks = new ArrayList<>();
        ParsedCourseBlock current = null;

        for (String line : lines) {
            Matcher courseMatcher = COURSE_ROW_PATTERN.matcher(line);
            if (courseMatcher.matches() && looksLikeCourseCode(courseMatcher.group(1))) {
                if (current != null) {
                    blocks.add(current);
                }
                current = new ParsedCourseBlock(courseMatcher.group(1), courseMatcher.group(2));
                continue;
            }

            if (current == null) {
                continue;
            }

            Matcher sectionMatcher = SECTION_AND_SCHEDULE_PATTERN.matcher(line);
            if (sectionMatcher.matches() && lineContainsDayName(sectionMatcher.group(2))) {
                current.seccion = sectionMatcher.group(1);
                parseSchedulesInto(current, sectionMatcher.group(2));
                continue;
            }

            if (lineContainsDayName(line)) {
                parseSchedulesInto(current, line);
                continue;
            }

            String normalizedLine = normalize(line);
            if (normalizedLine.contains("presencial") || normalizedLine.contains("virtual")) {
                current.modalidad = line.trim();
                continue;
            }

            if (current.profesor == null && line.contains(",")) {
                current.profesor = line.trim();
                continue;
            }

            if (current.profesor == null && looksLikeTeacherLine(line)) {
                current.profesor = line.trim();
                continue;
            }

            if (current.nombre == null || current.nombre.isBlank()) {
                current.nombre = line.trim();
                continue;
            }

            if (current.nombre.length() < 90) {
                current.nombre = (current.nombre + " " + line.trim()).trim();
            }
        }

        if (current != null) {
            blocks.add(current);
        }

        return blocks;
    }

    private void parseSchedulesInto(ParsedCourseBlock block, String value) {
        Matcher matcher = DAY_TIME_PATTERN.matcher(value);
        while (matcher.find()) {
            String day = matcher.group(1);
            String start = normalizeHour(matcher.group(2));
            String end = normalizeHour(matcher.group(3));
            block.horarios.add(new OnboardingPdfPreview.BloqueHorario(
                    mapDay(day),
                    start,
                    end,
                    matcher.group().trim()
            ));
        }
    }

    private Curso matchCourse(List<Curso> courses, ParsedCourseBlock block, String normalizedText) {
        String pdfCode = normalize(block.pdfCode);
        String pdfName = normalize(block.nombre);

        Optional<Curso> byShortCode = courses.stream()
                .filter(course -> {
                    String courseCode = normalize(course.codigo());
                    return courseCode.endsWith(pdfCode) || courseCode.contains(pdfCode);
                })
                .findFirst();
        if (byShortCode.isPresent()) {
            return byShortCode.get();
        }

        return courses.stream()
                .filter(course -> {
                    String courseName = normalize(course.nombre());
                    return !pdfName.isBlank() && (courseName.contains(pdfName) || pdfName.contains(courseName));
                })
                .findFirst()
                .orElseGet(() -> courses.stream()
                        .filter(course -> normalizedText.contains(normalize(course.nombre())))
                        .findFirst()
                        .orElse(null));
    }

    private List<String> splitLines(String value) {
        return Optional.ofNullable(value)
                .stream()
                .flatMap(text -> text.lines())
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private String extractFirstGroup(List<String> lines, Pattern pattern, int group) {
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(group).trim();
            }
        }
        return null;
    }

    private boolean looksLikeCourseCode(String value) {
        String compact = value == null ? "" : value.replaceAll("\\s+", "");
        return compact.length() >= 4 && compact.length() <= 6 && compact.matches(".*[A-Z].*") && compact.matches(".*\\d.*");
    }

    private boolean looksLikeTeacherLine(String line) {
        String normalized = normalize(line);
        return normalized.split(" ").length >= 3
                && !lineContainsDayName(line)
                && !normalized.contains("pabellon")
                && !normalized.contains("aula");
    }

    private boolean lineContainsDayName(String value) {
        String normalized = normalize(value);
        return normalized.contains("lunes")
                || normalized.contains("martes")
                || normalized.contains("miercoles")
                || normalized.contains("jueves")
                || normalized.contains("viernes")
                || normalized.contains("sabado")
                || normalized.contains("domingo");
    }

    private Integer mapDay(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "lunes" -> 1;
            case "martes" -> 2;
            case "miercoles" -> 3;
            case "jueves" -> 4;
            case "viernes" -> 5;
            case "sabado" -> 6;
            case "domingo" -> 7;
            default -> null;
        };
    }

    private String normalizeHour(String value) {
        try {
            return LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm")).toString();
        } catch (DateTimeParseException exception) {
            return value;
        }
    }

    private int scoreContains(String haystack, String needle) {
        if (needle == null || needle.isBlank()) {
            return 0;
        }
        if (haystack.contains(needle)) {
            return needle.length();
        }
        String compressedNeedle = needle.replace(" ", "");
        return haystack.replace(" ", "").contains(compressedNeedle) ? compressedNeedle.length() : 0;
    }

    private String extractYear(String normalizedText) {
        Matcher matcher = Pattern.compile("\\b(20\\d{2})\\b").matcher(normalizedText);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractMonth(String normalizedText) {
        for (String month : List.of("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")) {
            if (normalizedText.contains(month)) {
                return month;
            }
        }
        return null;
    }

    private String normalizePeriodo(String value) {
        String normalized = normalize(value);
        return normalized.replace('-', ' ');
    }

    private String normalize(String value) {
        return Optional.ofNullable(value)
                .map(item -> Normalizer.normalize(item, Normalizer.Form.NFD))
                .map(item -> item.replaceAll("\\p{M}+", ""))
                .map(item -> item.toLowerCase(Locale.ROOT))
                .map(item -> item.replaceAll("[^a-z0-9\\s-]", " "))
                .map(item -> item.replaceAll("\\s+", " "))
                .map(String::trim)
                .orElse("");
    }

    private record ScoredCampus(CampusCatalogo campus, int score) {
    }

    private record ScoredPeriodo(PeriodoCatalogo periodo, int score) {
    }

    private static final class ParsedCourseBlock {
        private final String pdfCode;
        private String nombre;
        private String profesor;
        private String seccion;
        private String modalidad;
        private final List<OnboardingPdfPreview.BloqueHorario> horarios = new ArrayList<>();

        private ParsedCourseBlock(String pdfCode, String nombre) {
            this.pdfCode = pdfCode;
            this.nombre = nombre == null ? "" : nombre.trim();
        }

        public String profesor() {
            return profesor;
        }

        public String seccion() {
            return seccion;
        }

        public String modalidad() {
            return modalidad;
        }

        public List<OnboardingPdfPreview.BloqueHorario> horarios() {
            return horarios;
        }
    }
}
