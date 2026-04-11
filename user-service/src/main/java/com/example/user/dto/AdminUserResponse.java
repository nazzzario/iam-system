package com.example.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String keycloakId,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        String role,
        LocalDateTime createdAt
) {}
