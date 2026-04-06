package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "User profile response")
public record UserResponse(
        @Schema(description = "Unique user identifier") UUID id,
        @Schema(description = "Keycloak user UUID (JWT sub claim)") String keycloakId,
        @Schema(description = "User email address") String email,
        @Schema(description = "User first name") String firstName,
        @Schema(description = "User last name") String lastName,
        @Schema(description = "Whether the user account is active") boolean isActive,
        @Schema(description = "Account creation timestamp") LocalDateTime createdAt
) {}
