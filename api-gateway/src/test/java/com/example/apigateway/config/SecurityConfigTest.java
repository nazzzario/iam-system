package com.example.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest
@Import(SecurityConfig.class)
@AutoConfigureWebTestClient
class SecurityConfigTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    ReactiveJwtDecoder jwtDecoder;

    @Test
    void health_endpoint_isPublic() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().value(status -> assertThat(status)
                        .isNotEqualTo(401)
                        .isNotEqualTo(403));
    }

    @Test
    void protectedRoute_withoutToken_returns401() {
        webTestClient.get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_withValidJwt_returns200OrForwarded() {
        webTestClient.mutateWith(mockJwt())
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isNotEqualTo(401));
    }
}
