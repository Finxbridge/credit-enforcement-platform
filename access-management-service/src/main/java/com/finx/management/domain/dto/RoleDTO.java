package com.finx.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    private String displayName;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Set<PermissionDTO> permissions;
}
