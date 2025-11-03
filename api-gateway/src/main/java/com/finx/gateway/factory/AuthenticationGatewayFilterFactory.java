package com.finx.gateway.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.gateway.dto.CommonResponse;
import com.finx.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthenticationGatewayFilterFactory(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    // This is a dummy config class
    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Define public endpoints that don't require authentication
            final List<String> publicEndpoints = List.of("/auth/login", "/auth/request-otp", "/auth/verify-otp",
                    "/auth/reset-password");

            Predicate<ServerHttpRequest> isPublic = r -> publicEndpoints.stream()
                    .anyMatch(uri -> r.getURI().getPath().contains(uri));

            if (isPublic.test(request)) {
                return chain.filter(exchange);
            }

            final String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return handleUnauthorized(exchange, "Missing or invalid Authorization header");
            }

            final String token = authHeader.substring(7);

            try {
                if (!jwtUtil.validateToken(token)) {
                    return handleUnauthorized(exchange, "JWT token is invalid or expired");
                }
            } catch (Exception e) {
                log.error("Error validating JWT token", e);
                return handleUnauthorized(exchange, "Error validating JWT token");
            }

            // Add user context to request headers for downstream services
            Claims claims = jwtUtil.getAllClaimsFromToken(token);
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.get("userId").toString())
                    .header("X-Username", claims.getSubject())
                    .header("X-Roles", claims.get("roles").toString())
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        log.warn(message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        CommonResponse<Object> responseBody = CommonResponse.failure(message, "UNAUTHORIZED");

        byte[] responseBytes = new byte[0];
        try {
            responseBytes = objectMapper.writeValueAsBytes(responseBody);
        } catch (JsonProcessingException e) {
            log.error("Error writing json response", e);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
