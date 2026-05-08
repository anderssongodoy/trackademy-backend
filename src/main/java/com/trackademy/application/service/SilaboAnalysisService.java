package com.trackademy.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.application.port.in.SilaboAnalysisUseCase;
import com.trackademy.application.port.out.SilaboAnalysisPort;
import com.trackademy.domain.model.SilaboAnalysis;
import com.trackademy.domain.model.SilaboAnalysisRecurso;
import com.trackademy.domain.model.SilaboParaAnalisis;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SilaboAnalysisService implements SilaboAnalysisUseCase {

    private static final Logger LOG = Logger.getLogger(SilaboAnalysisService.class);
    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    @ConfigProperty(name = "app.ai.anthropic.api-key", defaultValue = "")
    String apiKey;

    private final SilaboAnalysisPort silaboAnalysisPort;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public SilaboAnalysisService(SilaboAnalysisPort silaboAnalysisPort, ObjectMapper objectMapper) {
        this.silaboAnalysisPort = silaboAnalysisPort;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public SilaboAnalysis analizarSilabo(String email, Long usuarioPeriodoCursoId) {
        SilaboParaAnalisis silabo = silaboAnalysisPort
                .buscarSilaboPorUsuarioPeriodoCursoId(email, usuarioPeriodoCursoId)
                .orElseThrow(() -> new IllegalArgumentException("Silabo no disponible para este curso."));

        return silaboAnalysisPort.buscarAnalisisCacheado(silabo.hashPdf())
                .orElseGet(() -> {
                    SilaboAnalysis nuevo = callAnthropicAndParse(silabo);
                    return silaboAnalysisPort.guardarAnalisis(nuevo);
                });
    }

    private record ApiResult(String text, int inputTokens, int outputTokens) {}

    private SilaboAnalysis callAnthropicAndParse(SilaboParaAnalisis silabo) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("dummy-key")) {
            LOG.error("ANTHROPIC_API_KEY no configurada o es el valor por defecto");
            throw new IllegalStateException("Anthropic API key no configurada.");
        }

        LOG.infof("Llamando Anthropic API para silabo id=%d hashPdf=%s", silabo.silaboId(), silabo.hashPdf());
        ApiResult result = callApi(buildPrompt(silabo));
        return parseResponse(silabo, result);
    }

    private ApiResult callApi(String prompt) {
        try {
            var requestBody = Map.of(
                    "model", MODEL,
                    "max_tokens", 3000,
                    "tools", List.of(Map.of(
                            "type", "web_search_20250305",
                            "name", "web_search",
                            "max_uses", 3
                    )),
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    ))
            );

            String bodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("anthropic-beta", "web-search-2025-03-05")
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.errorf("Anthropic API error: status=%d body=%s", response.statusCode(), response.body());
                throw new RuntimeException("Anthropic API respondio con status " + response.statusCode());
            }

            LOG.infof("Anthropic API respondio OK, extrayendo texto y tokens");
            return extractApiResult(response.body());
        } catch (RuntimeException e) {
            LOG.errorf(e, "RuntimeException en callApi");
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Exception inesperada en callApi");
            throw new RuntimeException("Error llamando a Anthropic API: " + e.getMessage(), e);
        }
    }

    private ApiResult extractApiResult(String responseBody) throws Exception {
        var root = objectMapper.readTree(responseBody);
        var content = root.path("content");
        if (!content.isArray() || content.size() == 0) {
            throw new RuntimeException("Respuesta inesperada de Anthropic API");
        }

        String lastText = null;
        for (var block : content) {
            if ("text".equals(block.path("type").asText())) {
                lastText = block.path("text").asText();
            }
        }

        if (lastText == null || lastText.isBlank()) {
            throw new RuntimeException("Sin contenido de texto en la respuesta de Anthropic");
        }

        int inputTokens = root.path("usage").path("input_tokens").asInt(0);
        int outputTokens = root.path("usage").path("output_tokens").asInt(0);
        LOG.infof("Tokens usados — input=%d output=%d", inputTokens, outputTokens);

        return new ApiResult(lastText, inputTokens, outputTokens);
    }

    private SilaboAnalysis parseResponse(SilaboParaAnalisis silabo, ApiResult result) {
        String json = extractJson(result.text());
        try {
            var root = objectMapper.readTree(json);

            String resumen = root.path("resumen").asText("");

            List<String> temas = new ArrayList<>();
            root.path("temas").forEach(t -> temas.add(t.asText()));

            List<SilaboAnalysisRecurso> recursos = new ArrayList<>();
            root.path("recursos").forEach(r -> recursos.add(new SilaboAnalysisRecurso(
                    r.path("titulo").asText(""),
                    r.path("tipo").asText(""),
                    r.path("url").asText(""),
                    r.path("descripcion").asText("")
            )));

            List<String> paraIrMasAlla = new ArrayList<>();
            root.path("paraIrMasAlla").forEach(t -> paraIrMasAlla.add(t.asText()));

            return new SilaboAnalysis(
                    silabo.silaboId(), silabo.hashPdf(), resumen, temas, recursos, paraIrMasAlla,
                    result.inputTokens(), result.outputTokens(),
                    OffsetDateTime.now()
            );
        } catch (Exception e) {
            LOG.errorf(e, "Error parseando respuesta de IA. Texto recibido: %s", result.text());
            throw new RuntimeException("Error procesando respuesta de IA: " + e.getMessage(), e);
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String buildPrompt(SilaboParaAnalisis silabo) {
        var sb = new StringBuilder();
        sb.append("Eres un mentor académico que ayuda a estudiantes universitarios peruanos a sacarle el máximo provecho a sus cursos.\n\n");
        sb.append("SÍLABO: ").append(silabo.nombreCurso()).append("\n\n");

        if (silabo.sumilla() != null && !silabo.sumilla().isBlank())
            sb.append("Sumilla: ").append(silabo.sumilla()).append("\n\n");
        if (silabo.logroGeneral() != null && !silabo.logroGeneral().isBlank())
            sb.append("Logro general: ").append(silabo.logroGeneral()).append("\n\n");
        if (silabo.fundamentacion() != null && !silabo.fundamentacion().isBlank())
            sb.append("Fundamentación: ").append(silabo.fundamentacion()).append("\n\n");
        if (silabo.metodologia() != null && !silabo.metodologia().isBlank())
            sb.append("Metodología: ").append(silabo.metodologia()).append("\n\n");

        if (!silabo.unidades().isEmpty()) {
            sb.append("Unidades del curso:\n");
            silabo.unidades().forEach(u -> sb.append("- ").append(u).append("\n"));
            sb.append("\n");
        }

        sb.append("Completa estas 4 tareas en orden:\n\n");
        sb.append("TAREA 1 — resumen: Escribe 2-3 oraciones en español sobre qué enseña este curso y qué habilidades desarrolla el alumno.\n\n");
        sb.append("TAREA 2 — temas: Lista 5-7 conceptos o temas centrales del curso. Frases cortas y directas.\n\n");
        sb.append("TAREA 3 — paraIrMasAlla: Lista 4-5 tecnologías, skills o certificaciones que van MÁS ALLÁ de lo que cubre el sílabo y tienen valor real en el mercado laboral. ");
        sb.append("Usa TU PROPIO CONOCIMIENTO para esto — NO uses web_search aquí. ");
        sb.append("Piensa como un mentor senior: ¿qué le recomendarías aprender para destacar en el mundo laboral? ");
        sb.append("Formato: frases cortas y accionables. Ejemplos: 'Docker y despliegue en contenedores', 'Certificación CCNA (200-301)', 'Git avanzado y flujos de trabajo en equipo', 'TypeScript en proyectos reales'.\n\n");
        sb.append("TAREA 4 — recursos: Usa web_search para encontrar recursos reales y verificados:\n");
        sb.append("  · 3 videos de YouTube (tutoriales explicativos del tema, en español si existen)\n");
        sb.append("  · 1 libro gratuito online o PDF descargable\n");
        sb.append("  · 1 documentación oficial o referencia técnica\n");
        sb.append("  Verifica que las URLs existan. No inventes links.\n\n");
        sb.append("Cuando termines las 4 tareas, responde ÚNICAMENTE con este JSON (sin texto antes ni después):\n");
        sb.append("{\"resumen\":\"...\",\"temas\":[\"...\"],\"paraIrMasAlla\":[\"...\"],\"recursos\":[{\"titulo\":\"...\",\"tipo\":\"youtube|libro|documentacion\",\"url\":\"...\",\"descripcion\":\"...\"}]}");

        return sb.toString();
    }
}
