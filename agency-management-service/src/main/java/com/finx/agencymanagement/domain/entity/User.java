package com.finx.agencymanagement.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity - Read-only mapping to shared users table
 * User creation/management is done by access-management-service
 * This entity is used only for reading agent data for case assignment
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "max_case_capacity")
    private Integer maxCaseCapacity;

    @Column(name = "current_case_count")
    private Integer currentCaseCount;

    /**
     * Agency ID - Set when user has AGENT role.
     * Links agent to their agency.
     */
    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
