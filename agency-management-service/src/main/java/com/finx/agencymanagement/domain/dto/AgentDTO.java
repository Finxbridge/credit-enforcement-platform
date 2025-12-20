package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent DTO - represents an agent from users table
 * Used for dropdown selection when assigning cases to agents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String state;
    private String city;
    private Integer maxCaseCapacity;
    private Integer currentCaseCount;
    private String status;
}
