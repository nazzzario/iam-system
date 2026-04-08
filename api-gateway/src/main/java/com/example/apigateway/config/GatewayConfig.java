package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
class GatewayConfig {

    @Value("${USER_SERVICE_URL:http://user-service:8081}")
    private String userServiceUrl;

    @Bean
    GlobalFilter authorizationHeaderFilter() {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                return chain.filter(
                        exchange.mutate()
                                .request(r -> r.headers(h -> h.set(HttpHeaders.AUTHORIZATION, authHeader)))
                                .build()
                );
            }
            return chain.filter(exchange);
        };
    }

    @Bean
    RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/admin/**")
                        .uri(userServiceUrl))
                .build();
    }
}
