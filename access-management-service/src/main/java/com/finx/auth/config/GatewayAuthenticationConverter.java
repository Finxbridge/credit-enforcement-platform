package com.finx.auth.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GatewayAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String X_USERNAME_HEADER = "X-Username";
    private static final String X_ROLES_HEADER = "X-Roles";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(X_USERNAME_HEADER))
                .filter(username -> username != null && !username.isEmpty())
                .flatMap(username -> {
                    String rolesHeader = exchange.getRequest().getHeaders().getFirst(X_ROLES_HEADER);

                    List<SimpleGrantedAuthority> authorities = Arrays
                            .stream(rolesHeader != null ? rolesHeader.split(",") : new String[0])
                            .filter(role -> !role.trim().isEmpty())
                            .map(role -> new SimpleGrantedAuthority(role.trim()))
                            .collect(Collectors.toList());

                    // Create an authenticated token. The credentials can be null or a dummy value.
                    // The principal can be the username or a custom UserDetails object.
                    return Mono.just(new UsernamePasswordAuthenticationToken(username, null, authorities));
                });
    }
}
