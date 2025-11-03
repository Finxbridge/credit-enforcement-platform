package com.finx.management.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 8, max = 255)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

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
