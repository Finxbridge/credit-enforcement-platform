package com.finx.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPermissionDTO {
    private String code;
    private String name;
    private String category;
}
