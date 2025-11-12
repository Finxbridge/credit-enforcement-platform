package com.finx.management.repository;

import com.finx.management.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagementPermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionCode(String permissionCode);

    Optional<Permission> findByPermissionName(String permissionName);

    boolean existsByPermissionCode(String permissionCode);

    boolean existsByPermissionName(String permissionName);
}
