package com.trackademy.adapter.in.rest.dto;

public record ApiErrorResponse(
        String code,
        String message
) {
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }

    public static ApiErrorResponse validation(String message) {
        return of("validation_error", message);
    }

    public static ApiErrorResponse unauthorized(String message) {
        return of("unauthorized", message);
    }

    public static ApiErrorResponse forbidden(String message) {
        return of("forbidden", message);
    }

    public static ApiErrorResponse badRequest(String message) {
        return of("bad_request", message);
    }
}
