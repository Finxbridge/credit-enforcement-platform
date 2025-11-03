package com.finx.management.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private String displayName;

    @Size(max = 255)
    private String description;

    private Long roleGroupId;
    private Set<Long> permissionIds;
    private Boolean status;
}
