package com.trackademy.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface WhatsappWebhookUseCase {

    Optional<String> verifyWebhook(String mode, String verifyToken, String challenge);

    boolean isSignatureValid(byte[] rawPayload, String signatureHeader);

    void handleIncomingWebhook(JsonNode payload);
}
