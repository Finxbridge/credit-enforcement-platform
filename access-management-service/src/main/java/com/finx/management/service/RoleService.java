package com.finx.management.service;

import com.finx.management.domain.dto.CreateRoleRequest;
import com.finx.management.domain.dto.RoleDTO;
import com.finx.management.domain.dto.UpdateRoleRequest;

import java.util.List;

public interface RoleService {
    List<RoleDTO> getAllRoles();

    RoleDTO getRoleById(Long id);

    RoleDTO createRole(CreateRoleRequest request);

    RoleDTO updateRole(Long id, UpdateRoleRequest request);

    void deleteRole(Long id);
}
