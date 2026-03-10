package com.trackademy.adapter.in.rest;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Provider
@ApplicationScoped
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @ConfigProperty(name = "quarkus.http.cors", defaultValue = "true")
    boolean corsEnabled;

    @ConfigProperty(name = "quarkus.http.cors.origins", defaultValue = "https://trackademy.trinitylabs.app")
    String origins;

    @ConfigProperty(name = "quarkus.http.cors.headers", defaultValue = "accept,authorization,content-type,x-requested-with")
    String headers;

    @ConfigProperty(name = "quarkus.http.cors.methods", defaultValue = "GET,POST,PUT,DELETE,OPTIONS")
    String methods;

    @ConfigProperty(name = "quarkus.http.cors.access-control-allow-credentials", defaultValue = "true")
    boolean allowCredentials;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!corsEnabled) {
            return;
        }

        String origin = requestContext.getHeaderString("Origin");
        String requestMethod = requestContext.getHeaderString("Access-Control-Request-Method");
        if (origin == null) {
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod()) && requestMethod != null) {
            if (!isOriginAllowed(origin)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }

            Response.ResponseBuilder builder = Response.noContent();
            addCorsHeaders(builder, origin);
            requestContext.abortWith(builder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (!corsEnabled) {
            return;
        }

        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || !isOriginAllowed(origin)) {
            return;
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", Boolean.toString(allowCredentials));
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", headers);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", methods);
        responseContext.getHeaders().putSingle("Vary", "Origin");
    }

    private boolean isOriginAllowed(String origin) {
        Set<String> allowed = parseOrigins(origins);
        return allowed.contains(origin);
    }

    private static Set<String> parseOrigins(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    private void addCorsHeaders(Response.ResponseBuilder builder, String origin) {
        builder.header("Access-Control-Allow-Origin", origin);
        builder.header("Access-Control-Allow-Credentials", Boolean.toString(allowCredentials));
        builder.header("Access-Control-Allow-Headers", headers);
        builder.header("Access-Control-Allow-Methods", methods);
        builder.header("Vary", "Origin");
    }
}
