package com.example.user.service;

import com.example.user.dto.UpdateUserRequest;
import com.example.user.dto.UserResponse;
import com.example.user.entity.User;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .keycloakId("test-kc-id")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
    }

    @Test
    void getOrCreateUser_whenUserExists_returnsExistingUser() {
        when(userRepository.findByKeycloakId("kc-id")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getOrCreateUser("kc-id", "email@test.com");

        assertThat(result.email()).isEqualTo(testUser.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreateUser_whenUserNotExists_createsAndReturnsUser() {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .keycloakId("kc-id")
                .email("email@test.com")
                .build();

        when(userRepository.findByKeycloakId("kc-id")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        UserResponse result = userService.getOrCreateUser("kc-id", "email@test.com");

        assertThat(result.keycloakId()).isEqualTo("kc-id");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getCurrentUser_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findByKeycloakId("kc-id")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getCurrentUser("kc-id"));
    }

    @Test
    void updateCurrentUser_whenUserExists_updatesFields() {
        User updatedUser = User.builder()
                .id(testUser.getId())
                .keycloakId(testUser.getKeycloakId())
                .email(testUser.getEmail())
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        when(userRepository.findByKeycloakId("kc-id")).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(updatedUser);

        UserResponse result = userService.updateCurrentUser("kc-id", new UpdateUserRequest("John", "Doe"));

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
    }

    @Test
    void deactivateUser_whenUserExists_setsIsActiveFalse() {
        UUID id = testUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.deactivateUser(id);

        assertThat(testUser.isActive()).isFalse();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deactivateUser_whenUserNotFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deactivateUser(id));
    }
}
