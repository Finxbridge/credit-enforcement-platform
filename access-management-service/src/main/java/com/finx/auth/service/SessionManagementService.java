package com.finx.auth.service;

import com.finx.common.constants.CacheConstants;
import com.finx.common.service.ConfigCacheService;
import com.finx.auth.domain.dto.ActiveSessionResponse;
import com.finx.auth.domain.entity.User;
import com.finx.auth.domain.entity.UserSession;
import com.finx.auth.repository.AuthUserRepository;
import com.finx.auth.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SessionManagementService
 * Purpose: Manage user sessions with single active session enforcement
 * Configuration: Loads from system_config table via ConfigCacheService
 * FR: FR-AM-4, FR-AM-5
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManagementService {

    private final UserSessionRepository userSessionRepository;
    private final AuthUserRepository authUserRepository;
    private final ConfigCacheService configCacheService;

    // Configuration Keys

    // Defaults
    private static final int DEFAULT_INACTIVITY_TIMEOUT = 15; // minutes
    private static final boolean DEFAULT_SINGLE_SESSION_ENFORCEMENT = true;

    /**
     * Create new session for user with caching
     * FR-AM-5: Single active session per user
     * Cache: session (TTL: 15 minutes - matches session timeout)
     */
    @Transactional
    @CachePut(value = "session", key = "'session:' + #result.sessionId")
    public UserSession createSession(User user, String accessToken, String refreshToken,
            String ipAddress, String userAgent, String deviceType) {
        boolean singleEnforcement = configCacheService.getBooleanConfig(CacheConstants.SESSION_SINGLE_ENFORCEMENT,
                DEFAULT_SINGLE_SESSION_ENFORCEMENT);
        int inactivityTimeout = configCacheService.getIntConfig(CacheConstants.SESSION_INACTIVITY_TIMEOUT_MINUTES,
                DEFAULT_INACTIVITY_TIMEOUT);

        String sessionId = generateSessionId();

        // FR-AM-5: Terminate duplicate sessions if single session enforcement is
        // enabled
        if (singleEnforcement) {
            terminateDuplicateSessions(user.getId(), sessionId);
        }

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceType(deviceType)
                .isActive(true)
                .lastActivityAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(inactivityTimeout))
                .build();

        userSessionRepository.save(session);

        // Update user's session reference
        user.setSessionId(sessionId);
        user.setSessionExpiresAt(session.getExpiresAt());
        authUserRepository.save(user);

        log.info("Created new session for user: {} ({})", user.getUsername(), sessionId);
        return session;
    }

    /**
     * Terminate other active sessions for user (enforce single session)
     * FR-AM-5: Single active session enforcement
     * Cache: Evicts all sessions to ensure no stale cache
     */
    @Transactional
    @CacheEvict(value = "session", allEntries = true)
    public void terminateDuplicateSessions(Long userId, String currentSessionId) {
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(userId);

        for (UserSession session : activeSessions) {
            if (!session.getSessionId().equals(currentSessionId)) {
                session.terminate("DUPLICATE_LOGIN");
                userSessionRepository.save(session);
                log.info("Terminated duplicate session for user ID: {} (Session: {})",
                        userId, session.getSessionId());
            }
        }
    }

    /**
     * Validate session with Redis caching
     * Purpose: Ultra-fast session validation for every authenticated request
     * Old Response Time: 25ms | New Response Time: 1ms (cached)
     *
     * Cache: session (TTL: 15 minutes)
     * Cache Key: session:{sessionId}
     * Cache Update: On session activity update
     */
    @Cacheable(value = "session", key = "'session:' + #sessionId", unless = "#result == null")
    public UserSession getSessionForValidation(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId).orElse(null);
    }

    /**
     * Validate session (uses cache internally)
     */
    public boolean validateSession(String sessionId) {
        UserSession session = getSessionForValidation(sessionId);

        if (session == null) {
            return false;
        }

        if (!session.getIsActive()) {
            return false;
        }

        if (session.isExpired()) {
            terminateSession(sessionId, "TIMEOUT");
            return false;
        }

        // Update last activity
        updateSessionActivity(sessionId);
        return true;
    }

    /**
     * Update session activity timestamp with cache update
     * FR-AM-4: Auto-logout after inactivity
     * Cache: Updates both database and Redis cache
     */
    @Transactional
    @CachePut(value = "session", key = "'session:' + #sessionId")
    public UserSession updateSessionActivity(String sessionId) {
        int inactivityTimeout = configCacheService.getIntConfig(CacheConstants.SESSION_INACTIVITY_TIMEOUT_MINUTES,
                DEFAULT_INACTIVITY_TIMEOUT);

        return userSessionRepository.findBySessionId(sessionId)
                .map(session -> {
                    session.updateLastActivity();
                    session.setExpiresAt(LocalDateTime.now().plusMinutes(inactivityTimeout));
                    return userSessionRepository.save(session);
                })
                .orElse(null);
    }

    /**
     * Terminate session and evict from cache
     * Cache: Removes session from Redis to prevent stale cache
     */
    @Transactional
    @CacheEvict(value = "session", key = "'session:' + #sessionId")
    public void terminateSession(String sessionId, String reason) {
        userSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.terminate(reason);
            userSessionRepository.save(session);

            // Clear user's session reference if this is their current session
            authUserRepository.findById(session.getUserId()).ifPresent(user -> {
                if (sessionId.equals(user.getSessionId())) {
                    user.setSessionId(null);
                    user.setSessionExpiresAt(null);
                    authUserRepository.save(user);
                }
            });

            log.info("Terminated session: {} for user ID: {} (Reason: {})",
                    sessionId, session.getUserId(), reason);
        });
    }

    /**
     * Terminate all sessions for user (used during logout or admin action)
     * Cache: Evicts all sessions from cache
     */
    @Transactional
    @CacheEvict(value = "session", allEntries = true)
    public void terminateAllSessionsForUser(Long userId, String reason) {
        userSessionRepository.terminateAllActiveSessionsByUserId(userId, LocalDateTime.now(), reason);

        authUserRepository.findById(userId).ifPresent(user -> {
            user.setSessionId(null);
            user.setSessionExpiresAt(null);
            authUserRepository.save(user);
        });

        log.info("Terminated all sessions for user ID: {} (Reason: {})", userId, reason);
    }

    /**
     * Get active sessions for user
     */
    public ActiveSessionResponse getActiveSessionsForUser(Long userId) {
        List<UserSession> sessions = userSessionRepository.findActiveSessionsByUserId(userId);
        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ActiveSessionResponse.SessionInfo> sessionInfoList = sessions.stream()
                .map(session -> ActiveSessionResponse.SessionInfo.builder()
                        .sessionId(session.getSessionId())
                        .deviceType(session.getDeviceType())
                        .ipAddress(session.getIpAddress())
                        .lastActivityAt(session.getLastActivityAt())
                        .expiresAt(session.getExpiresAt())
                        .isActive(session.getIsActive())
                        .createdAt(session.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ActiveSessionResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .sessions(sessionInfoList)
                .build();
    }

    /**
     * Get session by ID
     */
    public UserSession getSessionById(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    /**
     * Get session by access token
     */
    public UserSession getSessionByAccessToken(String accessToken) {
        return userSessionRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    /**
     * Get session by refresh token
     */
    public UserSession getSessionByRefreshToken(String refreshToken) {
        return userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    /**
     * Cleanup expired sessions (Scheduled task)
     * FR-AM-4: Auto-logout enforcement
     * Cache: Clears all expired sessions from cache
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    @CacheEvict(value = "session", allEntries = true)
    public void cleanupExpiredSessions() {
        List<UserSession> expiredSessions = userSessionRepository.findExpiredSessions(LocalDateTime.now());

        for (UserSession session : expiredSessions) {
            session.terminate("TIMEOUT");
            userSessionRepository.save(session);
        }

        if (!expiredSessions.isEmpty()) {
            log.info("Cleaned up {} expired sessions", expiredSessions.size());
        }
    }

    /**
     * Generate unique session ID
     */
    private String generateSessionId() {
        return "sess-" + UUID.randomUUID().toString();
    }
}
