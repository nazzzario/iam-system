package com.example.user.repository;

import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);

    List<User> findAllByIsActiveTrue();

    long countByIsActiveTrue();

    long countByCreatedAtAfter(LocalDateTime date);
}
