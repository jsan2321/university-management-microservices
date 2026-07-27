package com.panadi.ums.security;

import java.time.LocalDateTime;

/**
 * Standardised error response returned by all microservices.
 * <p>Every {@code @RestControllerAdvice} in the system should return this record
 * so that API consumers receive a consistent error shape regardless of which
 * service handled the request.</p>
 *
 * @param code      machine-readable error code (e.g. {@code RESOURCE_NOT_FOUND})
 * @param message   human-readable description of the problem
 * @param status    HTTP status code (e.g. 404)
 * @param path      the request URI that triggered the error
 * @param timestamp when the error occurred
 */
public record ApiErrorResponse(
        String code,
        String message,
        int status,
        String path,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of(String code, String message, int status, String path) {
        return new ApiErrorResponse(code, message, status, path, LocalDateTime.now());
    }
}
