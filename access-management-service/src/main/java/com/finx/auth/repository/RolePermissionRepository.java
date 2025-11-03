package com.finx.auth.repository;

import com.finx.auth.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RolePermissionRepository
 * Purpose: Data access layer for RolePermission entity
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermissionId(Long permissionId);

    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<Long> findPermissionIdsByRoleId(Long roleId);

    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId IN :roleIds")
    List<Long> findPermissionIdsByRoleIds(List<Long> roleIds);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleId(Long roleId);
}
