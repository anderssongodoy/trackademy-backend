package com.trackademy.adapter.out.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.application.port.out.WhatsappMessagePort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MetaWhatsappMessageAdapter implements WhatsappMessagePort {

    private static final Logger LOG = Logger.getLogger(MetaWhatsappMessageAdapter.class);

    private final ObjectMapper objectMapper;
    private final String accessToken;
    private final String phoneNumberId;
    private final String apiVersion;
    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, Instant> lastSendByRecipient = new ConcurrentHashMap<>();

    public MetaWhatsappMessageAdapter(
            ObjectMapper objectMapper,
            @ConfigProperty(name = "app.whatsapp.meta.access-token", defaultValue = "") String accessToken,
            @ConfigProperty(name = "app.whatsapp.meta.phone-number-id", defaultValue = "") String phoneNumberId,
            @ConfigProperty(name = "app.whatsapp.meta.api-version", defaultValue = "v23.0") String apiVersion
    ) {
        this.objectMapper = objectMapper;
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.apiVersion = apiVersion;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void sendTextMessage(String to, String body) {
        if (to == null || to.isBlank() || body == null || body.isBlank()) {
            return;
        }
        if (accessToken == null || accessToken.isBlank() || phoneNumberId == null || phoneNumberId.isBlank()) {
            LOG.warn("WhatsApp message skipped because Meta config is incomplete.");
            return;
        }
        if (!canSendNow(to)) {
            LOG.warnf("WhatsApp message throttled for recipient %s", to);
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "messaging_product", "whatsapp",
                    "recipient_type", "individual",
                    "to", to,
                    "type", "text",
                    "text", Map.of(
                            "preview_url", false,
                            "body", body
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                LOG.errorf("WhatsApp send failed with status %d", response.statusCode());
                lastSendByRecipient.remove(to);
            }
        } catch (Exception ex) {
            lastSendByRecipient.remove(to);
            LOG.error("WhatsApp send failed due to integration error.", ex);
        }
    }

    private boolean canSendNow(String to) {
        Instant now = Instant.now();
        Instant previous = lastSendByRecipient.putIfAbsent(to, now);
        if (previous == null) {
            return true;
        }
        if (Duration.between(previous, now).getSeconds() < 6) {
            return false;
        }
        lastSendByRecipient.put(to, now);
        return true;
    }
}
