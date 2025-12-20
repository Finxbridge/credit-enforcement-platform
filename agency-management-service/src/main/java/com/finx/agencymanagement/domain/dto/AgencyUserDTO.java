package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agency User DTO
 * Matches frontend agency.types.ts AgencyUser interface
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyUserDTO {

    private Long id;
    private Long agencyId;
    private Long userId;  // Reference to main users table if applicable

    // Frontend uses employeeCode, backend has userCode
    @JsonProperty("employeeCode")
    private String employeeCode;

    private String designation;
    private String department;

    private String username;
    private String email;
    private String firstName;
    private String lastName;

    // Frontend uses phoneNumber, backend has mobileNumber
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
