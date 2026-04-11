package com.example.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull String role
) {}
