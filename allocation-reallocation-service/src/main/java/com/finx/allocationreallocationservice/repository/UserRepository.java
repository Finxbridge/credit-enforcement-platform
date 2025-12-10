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
     * Find all active agents for CAPACITY_BASED allocation
     * Sorted by current_case_count (ascending) for equalization - agents with fewer cases first
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.currentCaseCount ASC NULLS FIRST, u.id")
    List<User> findAllActiveAgents();

    /**
     * Find users by status
     */
    List<User> findByStatus(String status);

    /**
     * Find users by team ID
     */
    List<User> findByTeamId(Long teamId);

    /**
     * Find active users by state (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND LOWER(u.state) = LOWER(:state)")
    List<User> findByStateIgnoreCase(@Param("state") String state);

    /**
     * Find active users by city (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND LOWER(u.city) = LOWER(:city)")
    List<User> findByCityIgnoreCase(@Param("city") String city);

    /**
     * Find active users by state and city (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' " +
           "AND LOWER(u.state) = LOWER(:state) AND LOWER(u.city) = LOWER(:city)")
    List<User> findByStateAndCityIgnoreCase(@Param("state") String state, @Param("city") String city);

    /**
     * Find active users by state in list (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND LOWER(u.state) IN :states")
    List<User> findByStateInIgnoreCase(@Param("states") List<String> states);

    /**
     * Find active users by city in list (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND LOWER(u.city) IN :cities")
    List<User> findByCityInIgnoreCase(@Param("cities") List<String> cities);

    /**
     * Find active users by state and city in lists (case-insensitive)
     * Matches users where BOTH state AND city match
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' " +
           "AND LOWER(u.state) IN :states AND LOWER(u.city) IN :cities")
    List<User> findByStateAndCityInIgnoreCase(@Param("states") List<String> states, @Param("cities") List<String> cities);

    /**
     * Find active users by state list OR city list (case-insensitive)
     * Matches users where state matches OR city matches
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' " +
           "AND (LOWER(u.state) IN :states OR LOWER(u.city) IN :cities)")
    List<User> findByStateOrCityInIgnoreCase(@Param("states") List<String> states, @Param("cities") List<String> cities);

    /**
     * Find active users matching state OR city (case-insensitive)
     * Used for geography-based allocation - matches on state OR city
     * If both states and cities provided: matches users in those states OR those cities
     * If only states provided: matches users in those states
     * If only cities provided: matches users in those cities
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.status = 'ACTIVE' " +
           "AND ((:states IS NOT NULL AND LOWER(u.state) IN :states) " +
           "OR (:cities IS NOT NULL AND LOWER(u.city) IN :cities))")
    List<User> findByGeographyFiltersIgnoreCase(@Param("states") List<String> states,
                                                  @Param("cities") List<String> cities);
}
