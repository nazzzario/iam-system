package com.example.user.controller;

import com.example.user.dto.UserResponse;
import com.example.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            return http.build();
        }
    }

    private UserResponse testUserResponse() {
        return new UserResponse(
                UUID.randomUUID(),
                "kc-id",
                "test@test.com",
                "Test",
                "User",
                true,
                LocalDateTime.now()
        );
    }

    @Test
    void getMe_withValidJwt_returns200() throws Exception {
        when(userService.getOrCreateUser(eq("kc-id"), eq("test@test.com")))
                .thenReturn(testUserResponse());

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt().jwt(j -> j.subject("kc-id").claim("email", "test@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getMe_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMe_withValidRequest_returns200() throws Exception {
        when(userService.updateCurrentUser(any(), any()))
                .thenReturn(testUserResponse());

        mockMvc.perform(put("/api/v1/users/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Doe"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void updateMe_withBlankFirstName_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Doe"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
