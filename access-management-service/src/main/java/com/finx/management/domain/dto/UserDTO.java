package com.finx.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String status;
    private Long userGroupId;
    private String userGroupName;
    private String state;
    private String city;
    private List<String> assignedGeographies;
    private Integer maxCaseCapacity;
    private Integer currentCaseCount;
    private Double allocationPercentage;
    private String allocationBucket;
    private Long teamId;
    private Boolean isFirstLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<RoleDTO> roles;
    private Set<String> permissions;
}
