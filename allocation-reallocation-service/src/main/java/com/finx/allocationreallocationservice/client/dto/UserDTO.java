package com.finx.allocationreallocationservice.client.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String status;
    private String assignedGeographies;
    private Integer maxCaseCapacity;
    private Integer currentCaseCount;
    private Double allocationPercentage;
    private String allocationBucket;
    private Long teamId;

    // Additional fields for workload management
    private Integer capacity;
    private String geography;
}
