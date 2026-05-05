package com.trackademy.adapter.out.storage;

import com.trackademy.application.port.out.ImageUploadPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@ApplicationScoped
public class ImgbbImageUploadAdapter implements ImageUploadPort {

    private static final Logger LOG = Logger.getLogger(ImgbbImageUploadAdapter.class);
    private static final String IMGBB_API_BASE = "https://api.imgbb.com";

    private final String imgbbApiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImgbbImageUploadAdapter(
            @ConfigProperty(name = "app.upload.imgbb.api-key") Optional<String> imgbbApiKey
    ) {
        this.imgbbApiKey = imgbbApiKey.orElse("");
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String uploadBase64Image(String base64Image, String fileName) throws Exception {
        if (imgbbApiKey == null || imgbbApiKey.isBlank() || "dummy-test-key".equals(imgbbApiKey.trim())) {
            LOG.warn("ImgBB API key no configurado");
            throw new IOException("ImgBB API key is not configured");
        }

        try {
            String base64Clean = base64Image.contains(",") ? base64Image.split(",", 2)[1] : base64Image;

            String form = "key=" + URLEncoder.encode(imgbbApiKey, StandardCharsets.UTF_8)
                    + "&image=" + URLEncoder.encode(base64Clean, StandardCharsets.UTF_8)
                    + "&name=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IMGBB_API_BASE + "/1/upload"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                LOG.error("ImgBB responded with status: " + response.statusCode() + " body: " + response.body());
                throw new IOException("ImgBB upload failed with status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (root.path("success").asBoolean(false)) {
                String url = firstText(
                        root.at("/data/url"),
                        root.at("/data/display_url"),
                        root.at("/data/image/url")
                );
                if (url != null) {
                    LOG.info("Imagen subida a ImgBB: " + url);
                    return url;
                }
            }

            throw new IOException("ImgBB upload failed: " + response.body());
        } catch (Exception e) {
            LOG.error("Error uploading to ImgBB", e);
            throw new RuntimeException("Error uploading image: " + e.getMessage(), e);
        }
    }

    private String firstText(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && node.isTextual() && !node.asText().isBlank()) {
                return node.asText();
            }
        }
        return null;
    }
}
