package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying User table
 * Used to fetch user data from access-management database
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    /**
     * Find users by geography
     * Geography is stored as JSONB array in assigned_geographies column
     * Using PostgreSQL JSONB contains operator
     */
    @Query(value = "SELECT * FROM users WHERE status = 'ACTIVE' " +
           "AND jsonb_exists_any(assigned_geographies, CAST(:geographies AS text[]))",
           nativeQuery = true)
    List<User> findByGeographies(@Param("geographies") String[] geographies);

    /**
     * Find all active agents (users with role agent)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.id")
    List<User> findAllActiveUsers();

    /**
     * Find users by status
     */
    List<User> findByStatus(String status);

    /**
     * Find users by team ID
     */
    List<User> findByTeamId(Long teamId);
}
