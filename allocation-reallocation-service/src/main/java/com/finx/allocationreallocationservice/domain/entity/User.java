package com.finx.allocationreallocationservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Read-only entity for User table
 * Used for reading user data from access-management database
 */
@Entity
@Table(name = "users")
@Data
@Builder
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

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assigned_geographies", columnDefinition = "jsonb")
    private String assignedGeographies;

    @Column(name = "max_case_capacity")
    private Integer maxCaseCapacity;

    @Column(name = "current_case_count")
    private Integer currentCaseCount;

    @Column(name = "allocation_percentage", columnDefinition = "DECIMAL(5,2)")
    private Double allocationPercentage;

    @Column(name = "allocation_bucket", length = 50)
    private String allocationBucket;

    @Column(name = "team_id")
    private Long teamId;

    // Geography fields for allocation
    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "city", length = 100)
    private String city;

    /**
     * Agency ID - Present when user has AGENT role.
     * NULL for internal users (collectors, supervisors, managers, admins).
     * Used to filter out agency agents from first-level allocation.
     */
    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
