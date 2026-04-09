package com.trackademy.adapter.in.rest;

import com.trackademy.adapter.in.rest.dto.WhatsappLinkCodeResponse;
import com.trackademy.adapter.in.rest.dto.WhatsappLinkStatusResponse;
import com.trackademy.application.port.in.AuthUseCase;
import com.trackademy.application.port.in.WhatsappLinkUseCase;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/whatsapp")
@Produces(MediaType.APPLICATION_JSON)
public class WhatsappResource {

    private final WhatsappLinkUseCase whatsappLinkUseCase;
    private final AuthUseCase authUseCase;

    public WhatsappResource(WhatsappLinkUseCase whatsappLinkUseCase, AuthUseCase authUseCase) {
        this.whatsappLinkUseCase = whatsappLinkUseCase;
        this.authUseCase = authUseCase;
    }

    @POST
    @Path("/link-code")
    public Response generateLinkCode(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                WhatsappLinkCodeResponse.from(whatsappLinkUseCase.generateLinkCode(principal.get().email()))
        ).build();
    }

    @GET
    @Path("/link-status")
    public Response getLinkStatus(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(
                WhatsappLinkStatusResponse.from(whatsappLinkUseCase.getLinkStatus(principal.get().email()))
        ).build();
    }

    @DELETE
    @Path("/link")
    public Response unlink(@HeaderParam("Authorization") String authorization) {
        var principal = authUseCase.authenticate(authorization);
        if (principal.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        whatsappLinkUseCase.unlink(principal.get().email());
        return Response.noContent().build();
    }
}
