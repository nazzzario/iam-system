package com.example.user.dto;

public record StatsResponse(
        long totalUsers,
        long activeUsers,
        long newThisMonth
) {}
