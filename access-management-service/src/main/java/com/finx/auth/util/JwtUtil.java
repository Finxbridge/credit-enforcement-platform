package com.finx.auth.util;

import com.finx.auth.service.TokenBlacklistService;
import com.finx.common.constants.CacheConstants;
import com.finx.common.service.ConfigCacheService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Utility
 * Purpose: Generate and validate JWT tokens for authentication
 * Configuration: Loads from system_config table via ConfigCacheService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final ConfigCacheService configCacheService;
    private TokenBlacklistService tokenBlacklistService;

    private SecretKey key;

    /**
     * Set token blacklist service (injected after construction to avoid circular dependency)
     */
    public void setTokenBlacklistService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // Configuration Keys


    // Defaults (fallback if config not found)
    private static final String DEFAULT_JWT_SECRET = "MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS256Algorithm";
    private static final int DEFAULT_ACCESS_TOKEN_EXPIRATION = 15; // minutes
    private static final int DEFAULT_REFRESH_TOKEN_EXPIRATION = 7; // days
    private static final int DEFAULT_RESET_TOKEN_EXPIRATION = 10; // minutes

    @PostConstruct
    public void init() {
        // Load JWT secret from database config
        String jwtSecret = configCacheService.getConfigOrDefault(CacheConstants.JWT_SECRET, DEFAULT_JWT_SECRET);
        // Ensure the key is at least 256 bits for HS256
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT utility initialized with configuration from database");
    }

    /**
     * Generate Access Token (expiry from config)
     */
    public String generateAccessToken(Long userId, String username, String email, java.util.List<String> roles, java.util.List<String> permissions) {
        int expirationMinutes = configCacheService.getIntConfig(CacheConstants.JWT_ACCESS_TOKEN_EXPIRATION_MINUTES, DEFAULT_ACCESS_TOKEN_EXPIRATION);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("tokenType", "ACCESS");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000L))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generate Refresh Token (expiry from config)
     */
    public String generateRefreshToken(Long userId, String username) {
        int expirationDays = configCacheService.getIntConfig(CacheConstants.JWT_REFRESH_TOKEN_EXPIRATION_DAYS, DEFAULT_REFRESH_TOKEN_EXPIRATION);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "REFRESH");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationDays * 24 * 60 * 60 * 1000L))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generate Reset Token (expiry from config) - For password reset
     */
    public String generateResetToken(Long userId, String username, String requestId) {
        int expirationMinutes = configCacheService.getIntConfig(CacheConstants.JWT_RESET_TOKEN_EXPIRATION_MINUTES, DEFAULT_RESET_TOKEN_EXPIRATION);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("requestId", requestId); // OTP request ID
        claims.put("tokenType", "RESET");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000L))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validate token and return claims
     * Also checks if token is blacklisted
     */
    public Claims validateToken(String token) {
        try {
            // First check if token is blacklisted
            if (tokenBlacklistService != null && tokenBlacklistService.isTokenBlacklisted(token)) {
                log.error("JWT token is blacklisted (user logged out)");
                throw new RuntimeException("Token has been revoked");
            }

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token unsupported: {}", e.getMessage());
            throw new RuntimeException("Token unsupported");
        } catch (MalformedJwtException e) {
            log.error("JWT token malformed: {}", e.getMessage());
            throw new RuntimeException("Token malformed");
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw new RuntimeException("Token signature invalid");
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw new RuntimeException("Token invalid");
        }
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return validateToken(token).get("email", String.class);
    }

    /**
     * Extract token type
     */
    public String extractTokenType(String token) {
        return validateToken(token).get("tokenType", String.class);
    }

    /**
     * Extract OTP request ID from reset token
     */
    public String extractRequestId(String token) {
        return validateToken(token).get("requestId", String.class);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = validateToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get expiration time from token
     */
    public LocalDateTime getExpirationTime(String token) {
        Date expiration = validateToken(token).getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Get access token expiration time
     */
    public LocalDateTime getAccessTokenExpirationTime() {
        int expirationMinutes = configCacheService.getIntConfig(CacheConstants.JWT_ACCESS_TOKEN_EXPIRATION_MINUTES, DEFAULT_ACCESS_TOKEN_EXPIRATION);
        return LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    /**
     * Get refresh token expiration time
     */
    public LocalDateTime getRefreshTokenExpirationTime() {
        int expirationDays = configCacheService.getIntConfig(CacheConstants.JWT_REFRESH_TOKEN_EXPIRATION_DAYS, DEFAULT_REFRESH_TOKEN_EXPIRATION);
        return LocalDateTime.now().plusDays(expirationDays);
    }
}