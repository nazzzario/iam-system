package com.example.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/certs",
        "keycloak.admin.server-url=http://localhost:9999",
        "keycloak.admin.realm=test-realm",
        "keycloak.admin.client-id=test-client",
        "keycloak.admin.client-secret=test-secret"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
