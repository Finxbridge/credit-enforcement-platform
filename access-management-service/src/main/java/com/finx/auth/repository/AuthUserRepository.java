package com.finx.auth.repository;

import com.finx.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 * Purpose: Data access layer for User entity
 */
@Repository
public interface AuthUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findByStatus(String status);

    @Query("SELECT u FROM AuthUser u WHERE u.accountLockedUntil < :now AND u.status = 'LOCKED'")
    List<User> findExpiredLockedAccounts(LocalDateTime now);

    Optional<User> findBySessionId(String sessionId);
}
