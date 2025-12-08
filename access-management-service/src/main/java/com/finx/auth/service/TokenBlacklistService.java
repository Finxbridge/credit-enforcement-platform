package com.finx.auth.service;

import com.finx.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TokenBlacklistService
 * Purpose: Manage blacklisted tokens using Redis
 * When tokens are blacklisted, they can no longer be used even if they haven't expired
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * Add token to blacklist
     * Token will be stored in Redis until its natural expiration time
     */
    public void blacklistToken(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;

            // Get token expiration time
            LocalDateTime expirationTime = jwtUtil.getExpirationTime(token);
            LocalDateTime now = LocalDateTime.now();

            // Calculate remaining TTL
            long ttlSeconds = Duration.between(now, expirationTime).getSeconds();

            if (ttlSeconds > 0) {
                // Store in Redis with TTL equal to remaining token lifetime
                redisTemplate.opsForValue().set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
                log.debug("Token blacklisted successfully with TTL: {} seconds", ttlSeconds);
            } else {
                log.debug("Token already expired, skipping blacklist");
            }
        } catch (Exception e) {
            log.error("Error blacklisting token: {}", e.getMessage());
            // Store anyway with a default TTL of 24 hours as fallback
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", 24, TimeUnit.HOURS);
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * Remove token from blacklist (if needed for any reason)
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token removed from blacklist");
    }
}
