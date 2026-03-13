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
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
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
    private static final Pattern CICLO_PATTERN = Pattern.compile("\\bciclo\\s*[:\\-]?\\s*(\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREDITOS_PATTERN = Pattern.compile("\\b(\\d{1,2})\\s*creditos\\b", Pattern.CASE_INSENSITIVE);

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
        String normalizedText = normalize(rawText);

        List<String> advertencias = new ArrayList<>();
        List<CarreraCatalogo> carreras = catalogoAcademicoUseCase.listarCarreras(DEFAULT_UNIVERSITY_ID);
        CarreraCatalogo carrera = resolverCarrera(carreras);
        List<CampusCatalogo> campuses = catalogoAcademicoUseCase.listarCampuses(DEFAULT_UNIVERSITY_ID);
        List<PeriodoCatalogo> periodos = catalogoAcademicoUseCase.listarPeriodos(DEFAULT_UNIVERSITY_ID);
        List<Curso> cursos = carrera == null
                ? catalogoCursosUseCase.listarCursos()
                : catalogoCursosUseCase.listarCursosPorCarrera(carrera.id());

        CampusCatalogo campus = resolverCampus(campuses, normalizedText);
        PeriodoCatalogo periodo = resolverPeriodo(periodos, normalizedText);
        Integer cicloActual = resolverCiclo(rawText, normalizedText);
        List<OnboardingPdfPreview.CursoDetectado> cursosDetectados = resolverCursos(cursos, normalizedText);

        if (campus == null) {
            advertencias.add("No pudimos identificar el campus con suficiente confianza.");
        }
        if (periodo == null) {
            advertencias.add("No pudimos identificar el periodo del PDF.");
        }
        if (cicloActual == null) {
            advertencias.add("No pudimos identificar el ciclo actual. Puedes completarlo manualmente.");
        }
        if (cursosDetectados.isEmpty()) {
            advertencias.add("No encontramos cursos reconocibles en el PDF.");
        }

        return new OnboardingPdfPreview(
                carrera != null ? carrera.id() : null,
                carrera != null ? carrera.nombre() : null,
                campus != null ? campus.id() : null,
                campus != null ? campus.nombre() : null,
                periodo != null ? periodo.id() : null,
                periodo != null ? periodo.etiqueta() : null,
                cicloActual,
                cursosDetectados,
                advertencias
        );
    }

    private String extraerTexto(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private CarreraCatalogo resolverCarrera(List<CarreraCatalogo> carreras) {
        return carreras.stream()
                .filter(item -> normalize(item.nombre()).contains("ingenieria de sistemas"))
                .findFirst()
                .orElse(null);
    }

    private CampusCatalogo resolverCampus(List<CampusCatalogo> campuses, String normalizedText) {
        return campuses.stream()
                .map(campus -> new ScoredCampus(campus, scoreContains(normalizedText, normalize(campus.nombre()))))
                .filter(item -> item.score() > 0)
                .max(Comparator.comparingInt(ScoredCampus::score))
                .map(ScoredCampus::campus)
                .orElse(null);
    }

    private PeriodoCatalogo resolverPeriodo(List<PeriodoCatalogo> periodos, String normalizedText) {
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

    private Integer resolverCiclo(String rawText, String normalizedText) {
        Matcher directMatcher = CICLO_PATTERN.matcher(rawText);
        if (directMatcher.find()) {
            return Integer.parseInt(directMatcher.group(1));
        }

        Matcher normalizedMatcher = Pattern.compile("\\bnivel\\s*[:\\-]?\\s*(\\d{1,2})\\b", Pattern.CASE_INSENSITIVE)
                .matcher(rawText);
        if (normalizedMatcher.find()) {
            return Integer.parseInt(normalizedMatcher.group(1));
        }

        Matcher creditosMatcher = CREDITOS_PATTERN.matcher(normalizedText);
        if (creditosMatcher.find()) {
            return null;
        }

        return null;
    }

    private List<OnboardingPdfPreview.CursoDetectado> resolverCursos(List<Curso> cursos, String normalizedText) {
        LinkedHashSet<Long> detectedIds = new LinkedHashSet<>();
        List<OnboardingPdfPreview.CursoDetectado> detected = new ArrayList<>();

        for (Curso curso : cursos) {
            String normalizedCode = normalize(curso.codigo());
            if (normalizedCode.isBlank() || !normalizedText.contains(normalizedCode)) {
                continue;
            }
            if (!detectedIds.add(curso.id())) {
                continue;
            }
            detected.add(new OnboardingPdfPreview.CursoDetectado(curso.id(), curso.codigo(), curso.nombre()));
        }

        return detected;
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
}
