package com.finx.management.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

import java.util.Set;
import java.util.List;

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

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number. Must be 10 digits and start with 6-9.")
    @Size(max = 15)
    private String mobileNumber;

    private String status;
    private Long userGroupId;

    // Geography fields (mandatory for allocation)
    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    private List<String> assignedGeographies; // Optional additional geographies
    private Integer maxCaseCapacity;
    private Double allocationPercentage;
    private String allocationBucket;
    private Long teamId;

    private Set<Long> roleIds;
}
