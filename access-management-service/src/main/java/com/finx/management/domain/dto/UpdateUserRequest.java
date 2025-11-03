package com.finx.management.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserRequest {
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 15)
    private String mobileNumber;

    private String status;
    private Long userGroupId;
    private String assignedGeographies;
    private Integer maxCaseCapacity;
    private Double allocationPercentage;
    private String allocationBucket;
    private Long teamId;
    private Boolean isFirstLogin;
    private Set<Long> roleIds;
}
