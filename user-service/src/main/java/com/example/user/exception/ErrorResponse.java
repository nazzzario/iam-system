package com.example.user.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Error response body")
public record ErrorResponse(
        @Schema(description = "Error timestamp") Instant timestamp,
        @Schema(description = "HTTP status code") int status,
        @Schema(description = "Error message") String message,
        @Schema(description = "Request path that caused the error") String path
) {}
