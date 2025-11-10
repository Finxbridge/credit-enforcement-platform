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
    private static final String X_PERMISSIONS_HEADER = "X-Permissions";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(X_USERNAME_HEADER))
                .filter(username -> username != null && !username.isEmpty())
                .flatMap(username -> {
                    String rolesHeader = exchange.getRequest().getHeaders().getFirst(X_ROLES_HEADER);
                    String permissionsHeader = exchange.getRequest().getHeaders().getFirst(X_PERMISSIONS_HEADER);

                    List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

                    if (rolesHeader != null && !rolesHeader.isEmpty()) {
                        Arrays.stream(rolesHeader.split(","))
                                .filter(role -> !role.trim().isEmpty())
                                .map(role -> new SimpleGrantedAuthority(role.trim()))
                                .forEach(authorities::add);
                    }

                    if (permissionsHeader != null && !permissionsHeader.isEmpty()) {
                        Arrays.stream(permissionsHeader.split(","))
                                .filter(permission -> !permission.trim().isEmpty())
                                .map(permission -> new SimpleGrantedAuthority(permission.trim()))
                                .forEach(authorities::add);
                    }

                    // Create an authenticated token. The credentials can be null or a dummy value.
                    // The principal can be the username or a custom UserDetails object.
                    return Mono.just(new UsernamePasswordAuthenticationToken(username, null, authorities));
                });
    }
}
