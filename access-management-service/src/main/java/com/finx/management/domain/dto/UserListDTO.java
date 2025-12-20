package com.finx.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Simplified DTO for user list endpoint - contains only essential fields
 * for faster API response and reduced payload size
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserListDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String status;
    private String userGroupName;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
