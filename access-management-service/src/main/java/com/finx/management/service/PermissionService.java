package com.finx.management.service;

import com.finx.management.domain.dto.PermissionDTO;

import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();

    PermissionDTO getPermissionById(Long id);

    PermissionDTO createPermission(PermissionDTO permissionDTO);

    PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO);

    void deletePermission(Long id);
}
