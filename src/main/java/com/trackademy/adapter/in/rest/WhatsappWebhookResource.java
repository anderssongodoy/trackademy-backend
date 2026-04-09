package com.trackademy.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.application.port.in.WhatsappWebhookUseCase;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/webhooks/whatsapp")
public class WhatsappWebhookResource {

    private final WhatsappWebhookUseCase whatsappWebhookUseCase;
    private final ObjectMapper objectMapper;

    public WhatsappWebhookResource(WhatsappWebhookUseCase whatsappWebhookUseCase, ObjectMapper objectMapper) {
        this.whatsappWebhookUseCase = whatsappWebhookUseCase;
        this.objectMapper = objectMapper;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response verify(
            @QueryParam("hub.mode") String mode,
            @QueryParam("hub.verify_token") String verifyToken,
            @QueryParam("hub.challenge") String challenge
    ) {
        return whatsappWebhookUseCase.verifyWebhook(mode, verifyToken, challenge)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.FORBIDDEN))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receive(
            @HeaderParam("X-Hub-Signature-256") String signatureHeader,
            String rawPayload
    ) {
        byte[] rawBytes = rawPayload == null ? new byte[0] : rawPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (!whatsappWebhookUseCase.isSignatureValid(rawBytes, signatureHeader)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawPayload);
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        whatsappWebhookUseCase.handleIncomingWebhook(payload);
        return Response.ok().build();
    }
}
