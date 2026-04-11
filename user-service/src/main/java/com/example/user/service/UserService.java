package com.example.user.service;

import com.example.user.dto.AdminUpdateUserRequest;
import com.example.user.dto.AdminUserResponse;
import com.example.user.dto.CreateUserRequest;
import com.example.user.dto.StatsResponse;
import com.example.user.dto.UpdateUserRoleRequest;
import com.example.user.dto.UpdateUserRequest;
import com.example.user.dto.UserResponse;
import com.example.user.entity.User;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    UserService(UserRepository userRepository, KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getKeycloakId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    private AdminUserResponse toAdminResponse(User user, String role) {
        return new AdminUserResponse(
                user.getId(),
                user.getKeycloakId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                role,
                user.getCreatedAt()
        );
    }

    public UserResponse getOrCreateUser(String keycloakId, String email) {
        return userRepository.findByKeycloakId(keycloakId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakId(keycloakId)
                            .email(email)
                            .build();
                    return toResponse(userRepository.save(newUser));
                });
    }

    public UserResponse getCurrentUser(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .map(this::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + keycloakId));
    }

    public UserResponse updateCurrentUser(String keycloakId, UpdateUserRequest request) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + keycloakId));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return toResponse(userRepository.save(user));
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAllByIsActiveTrue().stream()
                .map(user -> toAdminResponse(user, keycloakAdminService.getUserRole(user.getKeycloakId())))
                .toList();
    }

    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setActive(false);
        userRepository.save(user);
        keycloakAdminService.updateUserStatus(user.getKeycloakId(), false);
    }

    public AdminUserResponse createUser(CreateUserRequest request) {
        List<String> roles = request.role().equals("USER")
                ? List.of("USER")
                : List.of("USER", request.role());

        String keycloakId = keycloakAdminService.createUser(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password(),
                roles
        );

        User user = User.builder()
                .keycloakId(keycloakId)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();

        return toAdminResponse(userRepository.save(user), request.role());
    }

    public AdminUserResponse updateUserRole(UUID id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        keycloakAdminService.updateUserRoles(user.getKeycloakId(), List.of("USER", request.role()));

        return toAdminResponse(user, request.role());
    }

    public AdminUserResponse activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setActive(true);
        keycloakAdminService.updateUserStatus(user.getKeycloakId(), true);

        return toAdminResponse(userRepository.save(user), null);
    }

    public StatsResponse getStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long newThisMonth = userRepository.countByCreatedAtAfter(
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        );
        return new StatsResponse(totalUsers, activeUsers, newThisMonth);
    }
}
