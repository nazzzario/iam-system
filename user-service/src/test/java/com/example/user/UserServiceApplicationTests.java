package com.example.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/iam-realm/protocol/openid-connect/certs"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
