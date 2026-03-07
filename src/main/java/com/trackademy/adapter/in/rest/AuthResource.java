package com.trackademy.adapter.in.rest;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final SecurityIdentity identity;

    public AuthResource(SecurityIdentity identity) {
        this.identity = identity;
    }

    @GET
    @Path("/session")
    public AuthSessionResponse session() {
        boolean authenticated = !identity.isAnonymous();
        String principal = authenticated ? identity.getPrincipal().getName() : null;

        return new AuthSessionResponse(authenticated, principal);
    }

    public record AuthSessionResponse(
            boolean authenticated,
            String principal
    ) {
    }
}
