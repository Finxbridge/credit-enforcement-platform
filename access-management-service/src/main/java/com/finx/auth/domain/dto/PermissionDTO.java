package com.finx.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission DTO
 * Purpose: Permission information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDTO {

    private Long id;
    private String permissionName;
    private String permissionCode;
    private String resource;
    private String action;
    private String description;
}
