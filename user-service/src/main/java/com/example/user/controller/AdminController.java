package com.example.user.controller;

import com.example.user.dto.AdminUserResponse;
import com.example.user.dto.CreateUserRequest;
import com.example.user.dto.StatsResponse;
import com.example.user.dto.UpdateUserRoleRequest;
import com.example.user.dto.UserResponse;
import com.example.user.exception.ErrorResponse;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin", description = "Admin user management")
class AdminController {

    private final UserService userService;

    AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all active users",
            description = "Returns a list of all active users. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active users returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user in Keycloak and the local database. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user role",
            description = "Replaces the user's realm roles in Keycloak. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AdminUserResponse> updateUserRole(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activate user",
            description = "Re-enables a previously deactivated user in Keycloak and local DB. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User activated successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AdminUserResponse> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deactivate user by ID",
            description = "Soft-deletes a user by setting isActive=false and disabling in Keycloak. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Get user statistics",
            description = "Returns total, active, and new-this-month user counts. Requires ADMIN or MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied — ADMIN or MANAGER role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(userService.getStats());
    }
}
