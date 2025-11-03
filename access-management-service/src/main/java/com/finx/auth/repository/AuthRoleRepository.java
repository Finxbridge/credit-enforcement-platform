package com.finx.auth.repository;

import com.finx.auth.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RoleRepository
 * Purpose: Data access layer for Role entity
 */
@Repository
public interface AuthRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);

    Optional<Role> findByRoleCode(String roleCode);

    List<Role> findByIsActive(Boolean isActive);

    Boolean existsByRoleName(String roleName);

    Boolean existsByRoleCode(String roleCode);

    long countByIdIn(List<Long> ids);
}
