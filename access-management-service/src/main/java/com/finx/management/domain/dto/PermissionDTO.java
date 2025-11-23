package com.finx.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDTO {
    private Long id;

    @NotBlank(message = "Permission code is required")
    @Size(max = 50, message = "Permission code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Resource is required")
    @Size(max = 100, message = "Resource must not exceed 100 characters")
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(max = 20, message = "Action must not exceed 20 characters")
    private String action;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private LocalDateTime createdAt;
}
