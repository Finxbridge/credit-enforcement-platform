package com.finx.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String X_USERNAME_HEADER = "X-Username";
    private static final String X_ROLES_HEADER = "X-Roles";

    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    public GatewayAuthFilter(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader(X_USERNAME_HEADER);
        String rolesHeader = request.getHeader(X_ROLES_HEADER);

        if (username != null && !username.isEmpty() && rolesHeader != null) {
            List<SimpleGrantedAuthority> rawAuthorities = Arrays.stream(rolesHeader.split(","))
                    .filter(role -> !role.trim().isEmpty())
                    .map(role -> new SimpleGrantedAuthority(role.trim()))
                    .collect(Collectors.toList());

            // Map the authorities using the injected mapper
            Collection<? extends GrantedAuthority> mappedAuthorities = grantedAuthoritiesMapper
                    .mapAuthorities(rawAuthorities);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, mappedAuthorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
