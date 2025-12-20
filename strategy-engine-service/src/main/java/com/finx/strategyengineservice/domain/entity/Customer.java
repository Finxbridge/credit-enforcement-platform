package com.finx.strategyengineservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only Customer entity for strategy execution
 * Maps to customers table in shared database
 * Updated to match unified CSV format
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

    // ==================== IDENTIFICATION ====================

    @Column(name = "customer_id", length = 50)
    private String customerId; // CUSTOMER ID

    @Column(name = "customer_code", unique = true, length = 50)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName; // CUSTOMER NAME

    // ==================== CONTACT INFORMATION ====================

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber; // MOBILE NO

    @Column(name = "secondary_mobile_number", length = 15)
    private String secondaryMobileNumber; // SECONDARY MOBILE NUMBER

    @Column(name = "resi_phone", length = 20)
    private String resiPhone; // RESI PHONE

    @Column(name = "additional_phone_2", length = 20)
    private String additionalPhone2; // ADDITIONAL PHONE 2

    @Column(name = "alternate_mobile", length = 15)
    private String alternateMobile;

    @Column(length = 100)
    private String email; // EMAIL

    @Column(name = "alternate_email", length = 100)
    private String alternateEmail;

    // ==================== ADDRESS ====================

    @Column(name = "primary_address", length = 500)
    private String primaryAddress; // PRIMARY ADDRESS

    @Column(name = "secondary_address", length = 500)
    private String secondaryAddress; // SECONDARY ADDRESS

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city; // CITY

    @Column(length = 100)
    private String state; // STATE

    @Column(length = 10)
    private String pincode; // PINCODE

    // ==================== PERSONAL DETAILS ====================

    @Column(name = "father_spouse_name", length = 255)
    private String fatherSpouseName; // FATHER SPOUSE NAME

    @Column(name = "employer_or_business_entity", length = 255)
    private String employerOrBusinessEntity; // EMPLOYER OR BUSINESS ENTITY

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 100)
    private String occupation;

    // ==================== REFERENCES ====================

    @Column(name = "reference_1_name", length = 255)
    private String reference1Name; // REFERENCE 1 NAME

    @Column(name = "reference_1_number", length = 20)
    private String reference1Number; // REFERENCE 1 NUMBER

    @Column(name = "reference_2_name", length = 255)
    private String reference2Name; // REFERENCE 2 NAME

    @Column(name = "reference_2_number", length = 20)
    private String reference2Number; // REFERENCE 2 NUMBER

    // ==================== STATUS ====================

    @Column(name = "customer_type", length = 20)
    private String customerType;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "language_preference", length = 20)
    private String languagePreference; // LANGUAGE

    // ==================== TIMESTAMPS ====================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
