package com.finx.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for GET /access/auth/me endpoint
 * Returns current authenticated user's details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentUserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String status;

    // Role information
    private String role;  // Primary role code (legacy support)
    private Set<RoleInfo> roles;
    private List<String> permissions;

    // Geography
    private String city;
    private String state;

    // Allocation
    private Integer maxCaseCapacity;
    private Integer currentCaseCount;
    private Double allocationPercentage;

    // Agency (for AGENT role)
    private Long agencyId;
    private String agencyName;

    // Login info
    private Boolean isFirstLogin;
    private LocalDateTime lastLoginAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String code;
        private String name;
        private String displayName;
    }
}
