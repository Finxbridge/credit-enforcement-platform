package com.finx.strategyengineservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only Customer entity for strategy execution
 * Maps to customers table in shared database
 * Used for accessing customer contact information for communications
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", unique = true, nullable = false, length = 50)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "alternate_mobile", length = 15)
    private String alternateMobile;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "alternate_email", length = 100)
    private String alternateEmail;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "customer_type", length = 20)
    private String customerType;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
