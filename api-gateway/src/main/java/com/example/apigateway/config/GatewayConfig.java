package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
class GatewayConfig {

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
}
