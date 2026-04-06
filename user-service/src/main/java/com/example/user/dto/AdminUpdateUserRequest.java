package com.example.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminUpdateUserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        Boolean isActive
) {}
