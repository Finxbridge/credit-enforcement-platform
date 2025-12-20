package com.finx.management.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

import java.util.List;
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

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number. Must be 10 digits and start with 6-9.")
    @Size(max = 15)
    private String mobileNumber;

    private String status;
    private Long userGroupId;

    // Geography fields
    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String city;

    private List<String> assignedGeographies; // Optional additional geographies
    private Integer maxCaseCapacity;
    private Double allocationPercentage;
    private String allocationBucket;
    private Long teamId;

    /**
     * Agency ID - Required when updating a user to have AGENT role.
     * Must reference an existing active agency.
     */
    private Long agencyId;

    private Set<Long> roleIds;
}
