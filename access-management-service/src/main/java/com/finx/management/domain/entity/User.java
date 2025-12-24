package com.finx.management.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity(name = "ManagementUser")
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

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    // Geography fields for allocation
    @Column(name = "state", length = 100)
    private String state; // e.g., "Telangana", "Maharashtra"

    @Column(name = "city", length = 100)
    private String city; // e.g., "Hyderabad", "Mumbai"

    // Additional geographies stored as JSONB (legacy/optional)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assigned_geographies", columnDefinition = "jsonb")
    private List<String> assignedGeographies;

    @Column(name = "max_case_capacity")
    private Integer maxCaseCapacity = 100;

    @Column(name = "current_case_count")
    private Integer currentCaseCount = 0;

    @Column(name = "allocation_percentage", columnDefinition = "DECIMAL(5,2)")
    private Double allocationPercentage = 100.00;

    @Column(name = "allocation_bucket", length = 50)
    private String allocationBucket;

    @Column(name = "team_id")
    private Long teamId;

    /**
     * Agency - Required when user has AGENT role.
     * References the agencies table.
     * NULL for internal users (collectors, supervisors, managers, admins).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "session_expires_at")
    private LocalDateTime sessionExpiresAt;

    @Column(name = "is_first_login")
    private Boolean isFirstLogin = false;

    @Column(name = "otp_secret")
    private String otpSecret;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED, PENDING_APPROVAL
    }
}
