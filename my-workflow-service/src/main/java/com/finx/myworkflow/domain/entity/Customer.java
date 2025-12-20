package com.finx.myworkflow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-only Customer entity for workflow
 * Includes comprehensive fields from case-sourcing CSV upload
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

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "secondary_mobile_number", length = 15)
    private String secondaryMobileNumber;

    @Column(name = "resi_phone", length = 20)
    private String resiPhone;

    @Column(name = "additional_phone_2", length = 20)
    private String additionalPhone2;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "primary_address", columnDefinition = "TEXT")
    private String primaryAddress;

    @Column(name = "secondary_address", columnDefinition = "TEXT")
    private String secondaryAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "father_spouse_name", length = 255)
    private String fatherSpouseName;

    @Column(name = "employer_or_business_entity", length = 255)
    private String employerOrBusinessEntity;

    @Column(name = "reference_1_name", length = 255)
    private String reference1Name;

    @Column(name = "reference_1_number", length = 20)
    private String reference1Number;

    @Column(name = "reference_2_name", length = 255)
    private String reference2Name;

    @Column(name = "reference_2_number", length = 20)
    private String reference2Number;

    @Column(name = "language_preference", length = 10)
    private String languagePreference;
}
