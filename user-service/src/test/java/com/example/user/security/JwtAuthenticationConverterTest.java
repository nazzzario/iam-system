package com.example.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthenticationConverter();
    }

    @Test
    void convert_withValidRoles_returnsCorrectAuthorities() {
        Jwt jwt = buildJwt(Map.of("realm_access", Map.of("roles", List.of("USER", "ADMIN"))));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).hasSize(2);
        assertThat(token.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void convert_withMissingRealmAccess_returnsEmptyAuthorities() {
        Jwt jwt = buildJwt(Map.of("sub", "test-subject"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void convert_withEmptyRoles_returnsEmptyAuthorities() {
        Jwt jwt = buildJwt(Map.of("realm_access", Map.of("roles", List.of())));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claims(c -> c.putAll(claims))
                .build();
    }
}
