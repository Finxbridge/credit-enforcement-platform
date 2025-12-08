package com.finx.management.repository;

import com.finx.management.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Check if a role is assigned to any active users
     */
    @Query("SELECT COUNT(u) > 0 FROM ManagementUser u JOIN u.roles r WHERE r.id = :roleId AND u.status = 'ACTIVE'")
    boolean existsByRoleIdAndActiveUsers(@Param("roleId") Long roleId);

    /**
     * Count active users assigned to a specific role
     */
    @Query("SELECT COUNT(u) FROM ManagementUser u JOIN u.roles r WHERE r.id = :roleId AND u.status = 'ACTIVE'")
    long countActiveUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * Optimized query to fetch user with roles and permissions in single query
     * Avoids N+1 and Cartesian product issues
     */
    @Query("SELECT DISTINCT u FROM ManagementUser u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") Long userId);
}
