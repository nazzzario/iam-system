package com.example.user.service;

import com.example.user.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
class KeycloakAdminService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    KeycloakAdminService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    String createUser(String email, String firstName, String lastName,
                      String password, List<String> roles) {
        try {
            log.debug("Creating user in Keycloak: email={}", email);

            UserRepresentation user = new UserRepresentation();
            user.setEmail(email);
            user.setUsername(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(true);
            user.setCredentials(List.of(credential));

            try (Response response = keycloak.realm(realm).users().create(user)) {
                log.debug("Keycloak response status: {}", response.getStatus());

                if (response.getStatus() == 409) {
                    log.error("Keycloak error response: {}", response.readEntity(String.class));
                    throw new UserAlreadyExistsException("User already exists with email: " + email);
                }
                if (response.getStatus() != 201) {
                    log.error("Keycloak error response: {}", response.readEntity(String.class));
                    throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
                }

                String location = response.getHeaderString("Location");
                String keycloakId = location.substring(location.lastIndexOf("/") + 1);

                assignRoles(keycloakId, roles);

                return keycloakId;
            }
        } catch (RuntimeException e) {
            log.error("Failed to create user in Keycloak: email={}", email, e);
            throw e;
        }
    }

    void assignRoles(String keycloakId, List<String> roleNames) {
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> keycloak.realm(realm).roles().get(name).toRepresentation())
                .toList();
        keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().add(roles);
    }

    void updateUserStatus(String keycloakId, boolean enabled) {
        UserRepresentation user = keycloak.realm(realm).users().get(keycloakId).toRepresentation();
        user.setEnabled(enabled);
        keycloak.realm(realm).users().get(keycloakId).update(user);
    }

    void updateUserRoles(String keycloakId, List<String> newRoles) {
        List<RoleRepresentation> currentRoles = keycloak.realm(realm)
                .users().get(keycloakId).roles().realmLevel().listAll();
        keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().remove(currentRoles);

        assignRoles(keycloakId, newRoles);
    }

    private static final List<String> DEFAULT_ROLES =
            List.of("default-roles-iam-realm", "offline_access", "uma_authorization");

    String getUserRole(String keycloakId) {
        return keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(name -> !DEFAULT_ROLES.contains(name))
                .findFirst()
                .orElse("USER");
    }
}
