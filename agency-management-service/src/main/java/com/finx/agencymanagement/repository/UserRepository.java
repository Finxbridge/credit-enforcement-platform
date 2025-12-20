package com.finx.agencymanagement.repository;

import com.finx.agencymanagement.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User Repository - Read-only access to shared users table
 * User creation/management is done by access-management-service
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Get all active agents assigned to any agency (users with agency_id set)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.agencyId IS NOT NULL")
    List<User> findAllActiveAgents();

    /**
     * Get agents belonging to a specific agency (users with agency_id = agencyId)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.agencyId = :agencyId")
    List<User> findAgentsByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Increment current case count for user
     */
    @Modifying
    @Query("UPDATE User u SET u.currentCaseCount = u.currentCaseCount + :count WHERE u.id = :userId")
    void incrementCaseCount(@Param("userId") Long userId, @Param("count") int count);

    /**
     * Decrement current case count for user
     */
    @Modifying
    @Query("UPDATE User u SET u.currentCaseCount = CASE WHEN u.currentCaseCount - :count < 0 THEN 0 ELSE u.currentCaseCount - :count END WHERE u.id = :userId")
    void decrementCaseCount(@Param("userId") Long userId, @Param("count") int count);
}
