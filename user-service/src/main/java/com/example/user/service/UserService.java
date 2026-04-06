package com.example.user.service;

import com.example.user.dto.AdminUpdateUserRequest;
import com.example.user.dto.UpdateUserRequest;
import com.example.user.dto.UserResponse;
import com.example.user.entity.User;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setActive(false);
        userRepository.save(user);
    }
}
