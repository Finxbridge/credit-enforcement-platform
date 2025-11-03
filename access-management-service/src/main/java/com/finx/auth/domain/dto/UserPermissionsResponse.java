package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Permissions Response DTO
 * Purpose: Return user's permissions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionsResponse {

    private Long userId;
    private String username;
    private List<RoleDTO> roles;
    private List<PermissionDTO> permissions;
}
