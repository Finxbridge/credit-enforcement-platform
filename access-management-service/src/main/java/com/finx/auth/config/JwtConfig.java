package com.finx.auth.config;

import com.finx.auth.service.TokenBlacklistService;
import com.finx.auth.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration
 * Purpose: Configure JWT utility dependencies to avoid circular dependency
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @PostConstruct
    public void init() {
        jwtUtil.setTokenBlacklistService(tokenBlacklistService);
    }
}
