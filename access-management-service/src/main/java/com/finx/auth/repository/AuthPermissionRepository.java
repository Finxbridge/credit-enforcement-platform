package com.finx.auth.repository;

import com.finx.auth.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PermissionRepository
 * Purpose: Data access layer for Permission entity
 */
@Repository
public interface AuthPermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionName(String permissionName);

    Optional<Permission> findByPermissionCode(String permissionCode);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceAndAction(String resource, String action);

    Boolean existsByPermissionCode(String permissionCode);

    long countByIdIn(List<Long> ids);
}
