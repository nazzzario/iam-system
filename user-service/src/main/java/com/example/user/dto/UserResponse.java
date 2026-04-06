package com.example.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String keycloakId,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        LocalDateTime createdAt
) {}
