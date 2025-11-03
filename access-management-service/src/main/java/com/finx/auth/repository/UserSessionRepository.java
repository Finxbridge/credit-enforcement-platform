package com.finx.auth.repository;

import com.finx.auth.domain.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserSessionRepository
 * Purpose: Data access layer for UserSession entity
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    Optional<UserSession> findByAccessToken(String accessToken);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserId(Long userId);

    List<UserSession> findByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query("SELECT us FROM UserSession us WHERE us.userId = :userId AND us.isActive = true")
    List<UserSession> findActiveSessionsByUserId(Long userId);

    @Query("SELECT us FROM UserSession us WHERE us.expiresAt < :now AND us.isActive = true")
    List<UserSession> findExpiredSessions(LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false, us.terminatedAt = :now, us.terminationReason = :reason WHERE us.userId = :userId AND us.isActive = true")
    void terminateAllActiveSessionsByUserId(Long userId, LocalDateTime now, String reason);

    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false, us.terminatedAt = :now, us.terminationReason = :reason WHERE us.userId = :userId AND us.sessionId != :currentSessionId AND us.isActive = true")
    void terminateOtherActiveSessionsByUserId(Long userId, String currentSessionId, LocalDateTime now, String reason);

    @Modifying
    @Query("UPDATE UserSession us SET us.lastActivityAt = :now WHERE us.sessionId = :sessionId")
    void updateLastActivity(String sessionId, LocalDateTime now);
}
