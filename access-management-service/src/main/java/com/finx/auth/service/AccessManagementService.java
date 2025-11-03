package com.finx.auth.service;

import com.finx.common.exception.ResourceNotFoundException;
import com.finx.auth.domain.entity.Role;
import com.finx.auth.domain.entity.User;
import com.finx.auth.domain.entity.UserRole;
import com.finx.auth.repository.AuthRoleRepository;
import com.finx.auth.repository.AuthUserRepository;
import com.finx.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AccessManagementService
 * Purpose: Manage user roles and permissions with automatic cache invalidation
 *
 * Features:
 * - Assign/revoke roles to/from users
 * - Automatic cache eviction for user permissions
 * - Ensures cache consistency when roles change
 *
 * Cache Eviction Strategy:
 * - When user roles change → evict user_permissions cache for that user
 * - When role permissions change → evict all user_permissions (via separate
 * service)
 *
 * Performance Impact:
 * - Cache eviction is critical to prevent stale permissions
 * - Without eviction, users might have outdated access rights
 *
 * FR: FR-AM-7, FR-AM-8
 *
 * @author CMS-NMS Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessManagementService {

    private final AuthUserRepository authUserRepository;
    private final AuthRoleRepository authRoleRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Assign role to user
     * Automatically evicts user permissions cache
     *
     * @param userId User ID
     * @param roleId Role ID
     */
    @Transactional
    @CacheEvict(value = "user_permissions", key = "'user:permissions:' + #userId")
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Validate role exists
        Role role = authRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        // Check if role already assigned
        if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            log.warn("Role {} already assigned to user {}", roleId, userId);
            return;
        }

        // Create user role assignment
        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .assignedAt(LocalDateTime.now())
                .build();

        userRoleRepository.save(userRole);

        log.info("Successfully assigned role {} ({}) to user {} ({}). Cache evicted.",
                role.getRoleCode(), roleId, user.getUsername(), userId);
    }

    /**
     * Revoke role from user
     * Automatically evicts user permissions cache
     *
     * @param userId User ID
     * @param roleId Role ID
     */
    @Transactional
    @CacheEvict(value = "user_permissions", key = "'user:permissions:' + #userId")
    public void revokeRoleFromUser(Long userId, Long roleId) {
        log.info("Revoking role {} from user {}", roleId, userId);

        // Validate user exists
        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Validate role exists
        Role role = authRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        // Find and delete user role
        userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .ifPresentOrElse(
                        userRole -> {
                            userRoleRepository.delete(userRole);
                            log.info("Successfully revoked role {} ({}) from user {} ({}). Cache evicted.",
                                    role.getRoleCode(), roleId, user.getUsername(), userId);
                        },
                        () -> log.warn("Role {} was not assigned to user {}", roleId, userId));
    }

    @Transactional
    @CacheEvict(value = "user_permissions", key = "'user:permissions:' + #userId")
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("Assigning {} roles to user {}", roleIds.size(), userId);

        // Validate user exists
        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Efficiently validate all roles exist in one query
        List<Role> validRoles = authRoleRepository.findAllById(roleIds);
        if (validRoles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more roles not found.");
        }

        int assignedCount = 0;
        for (Long roleId : roleIds) {
            // Check if already assigned
            if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
                log.debug("Role {} already assigned to user {}, skipping", roleId, userId);
                continue;
            }

            // Create user role assignment
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .assignedAt(LocalDateTime.now())
                    .build();

            userRoleRepository.save(userRole);
            assignedCount++;
        }

        log.info("Successfully assigned {} roles to user {} ({}). Cache evicted.",
                assignedCount, user.getUsername(), userId);
    }

    /**
     * Replace all user roles
     * Removes existing roles and assigns new ones
     * Evicts user permissions cache once
     *
     * @param userId  User ID
     * @param roleIds List of new role IDs
     */
    @Transactional
    @CacheEvict(value = "user_permissions", key = "'user:permissions:' + #userId")
    public void replaceUserRoles(Long userId, List<Long> roleIds) {
        log.info("Replacing roles for user {} with {} new roles", userId, roleIds.size());

        // Validate user exists
        User user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Efficiently validate all roles exist in one query
        if (roleIds != null && !roleIds.isEmpty()) {
            long validRolesCount = authRoleRepository.countByIdIn(roleIds);
            if (validRolesCount != roleIds.size()) {
                throw new ResourceNotFoundException("One or more roles not found.");
            }
        }

        // Remove all existing roles
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(existingRoles);
        log.info("Removed {} existing roles from user {}", existingRoles.size(), userId);

        // Assign new roles
        int assignedCount = 0;
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                UserRole userRole = UserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .assignedAt(LocalDateTime.now())
                        .build();
                userRoleRepository.save(userRole);
                assignedCount++;
            }
        }

        log.info("Successfully replaced roles for user {} ({}). Assigned {} roles. Cache evicted.",
                user.getUsername(), userId, assignedCount);
    }

    /**
     * Evict permissions cache for specific user
     * Useful for manual cache invalidation
     *
     * @param userId User ID
     */
    @CacheEvict(value = "user_permissions", key = "'user:permissions:' + #userId")
    public void evictUserPermissionsCache(Long userId) {
        log.info("Manually evicted permissions cache for user {}", userId);
    }

    /**
     * Evict permissions cache for all users
     * Use when role definitions or role permissions change
     *
     * Impact: Forces all users to reload permissions on next request
     * Performance: Safe - only affects cache, not database
     */
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void evictAllUserPermissionsCache() {
        log.info("Evicted permissions cache for ALL users");
    }

    /**
     * Get user roles (helper method)
     *
     * @param userId User ID
     * @return List of roles assigned to user
     */
    public List<UserRole> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId);
    }

    /**
     * Check if user has specific role
     *
     * @param userId User ID
     * @param roleId Role ID
     * @return true if user has the role
     */
    public boolean userHasRole(Long userId, Long roleId) {
        return userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
    }
}
