package com.finx.auth.service;

import com.finx.common.exception.ResourceNotFoundException;
import com.finx.auth.domain.entity.Permission;
import com.finx.auth.domain.entity.Role;
import com.finx.auth.domain.entity.RolePermission;
import com.finx.auth.repository.AuthPermissionRepository;
import com.finx.auth.repository.RolePermissionRepository;
import com.finx.auth.repository.AuthRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PermissionManagementService
 * Purpose: Manage role permissions with automatic cache invalidation
 *
 * Features:
 * - Assign/revoke permissions to/from roles
 * - Automatic cache eviction for ALL user permissions (since role permissions
 * affect all users with that role)
 *
 * Cache Eviction Strategy:
 * - When role permissions change → evict ALL user_permissions cache
 * via @CacheEvict
 * - Why evict all? Because any user with that role needs updated permissions
 * - Performance: Safe - users reload permissions on next request (cached for 6
 * hours)
 *
 * FR: FR-AM-7, FR-AM-8
 *
 * @author CMS-NMS Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionManagementService {

    private final AuthRoleRepository authRoleRepository;
    private final AuthPermissionRepository authPermissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Assign permission to role
     * Evicts ALL user permissions cache (affects all users with this role)
     *
     * @param roleId       Role ID
     * @param permissionId Permission ID
     */
    @Transactional
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = authRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        Permission permission = authPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            log.warn("Permission {} already assigned to role {}", permissionId, roleId);
            return;
        }

        // Create role permission assignment
        RolePermission rolePermission = RolePermission.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .grantedAt(LocalDateTime.now())
                .build();

        rolePermissionRepository.save(rolePermission);

        // Also invalidate role permissions cache in UserPermissionCacheService

        log.info("Successfully assigned permission {} ({}) to role {} ({}). ALL user permissions cache evicted.",
                permission.getPermissionCode(), permissionId, role.getRoleCode(), roleId);
    }

    /**
     * Revoke permission from role
     * Evicts ALL user permissions cache
     *
     * @param roleId       Role ID
     * @param permissionId Permission ID
     */
    @Transactional
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void revokePermissionFromRole(Long roleId, Long permissionId) {
        Role role = authRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        Permission permission = authPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));

        // Find and delete role permission
        rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
                .ifPresentOrElse(
                        rolePermission -> {
                            rolePermissionRepository.delete(rolePermission);

                            // Also invalidate role permissions cache in UserPermissionCacheService

                            log.info(
                                    "Successfully revoked permission {} ({}) from role {} ({}). ALL user permissions cache evicted.",
                                    permission.getPermissionCode(), permissionId, role.getRoleCode(), roleId);
                        },
                        () -> log.warn("Permission {} was not assigned to role {}", permissionId, roleId));
    }

    /**
     * Assign multiple permissions to role
     * Evicts ALL user permissions cache once (efficient bulk operation)
     *
     * @param roleId        Role ID
     * @param permissionIds List of permission IDs
     */
    @Transactional
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        log.info("Assigning {} permissions to role {}", permissionIds.size(), roleId);

        // Validate role exists
        Role role = authRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        long validPermissionsCount = authPermissionRepository.countByIdIn(permissionIds);
        if (validPermissionsCount != permissionIds.size()) {
            throw new ResourceNotFoundException("One or more permissions not found.");
        }

        int assignedCount = 0;
        for (Long permissionId : permissionIds) {
            // Check if already assigned
            if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                log.debug("Permission {} already assigned to role {}, skipping", permissionId, roleId);
                continue;
            }

            // Create role permission assignment
            RolePermission rolePermission = RolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permissionId)
                    .grantedAt(LocalDateTime.now())
                    .build();

            rolePermissionRepository.save(rolePermission);
            assignedCount++;
        }

        // Invalidate role permissions cache in UserPermissionCacheService

        log.info("Successfully assigned {} permissions to role {} ({}). ALL user permissions cache evicted.",
                assignedCount, role.getRoleCode(), roleId);
    }

    /**
     * Replace all role permissions
     * Removes existing permissions and assigns new ones
     * Evicts ALL user permissions cache
     *
     * @param roleId        Role ID
     * @param permissionIds List of new permission IDs
     */
    @Transactional
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
        log.info("Replacing permissions for role {} with {} new permissions", roleId, permissionIds.size());

        // Validate role exists
        Role role = authRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        // Efficiently validate all permissions exist in one query
        if (permissionIds != null && !permissionIds.isEmpty()) {
            long validPermissionsCount = authPermissionRepository.countByIdIn(permissionIds);
            if (validPermissionsCount != permissionIds.size()) {
                throw new ResourceNotFoundException("One or more permissions not found.");
            }
        }

        // Remove all existing permissions
        List<RolePermission> existingPermissions = rolePermissionRepository.findByRoleId(roleId);
        rolePermissionRepository.deleteAll(existingPermissions);
        log.info("Removed {} existing permissions from role {}", existingPermissions.size(), roleId);

        // Assign new permissions
        int assignedCount = 0;
        if (permissionIds != null) {
            for (Long permissionId : permissionIds) {
                RolePermission rolePermission = RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .grantedAt(LocalDateTime.now())
                        .build();
                rolePermissionRepository.save(rolePermission);
                assignedCount++;
            }
        }

        // Invalidate role permissions cache in UserPermissionCacheService

        log.info(
                "Successfully replaced permissions for role {} ({}). Assigned {} permissions. ALL user permissions cache evicted.",
                role.getRoleCode(), roleId, assignedCount);
    }

    /**
     * Refresh role permissions cache
     * Reloads all role permissions from database
     *
     * @return Number of roles refreshed
     */
    @CacheEvict(value = "user_permissions", allEntries = true)
    public int refreshRolePermissionsCache() {
        log.info("Refreshing role permissions cache - cache evicted");
        // Cache will be rebuilt on next access
        return authRoleRepository.findAll().size();
    }

    /**
     * Get role permissions (helper method)
     *
     * @param roleId Role ID
     * @return List of permissions assigned to role
     */
    public List<RolePermission> getRolePermissions(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    /**
     * Check if role has specific permission
     *
     * @param roleId       Role ID
     * @param permissionId Permission ID
     * @return true if role has the permission
     */
    public boolean roleHasPermission(Long roleId, Long permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }
}
