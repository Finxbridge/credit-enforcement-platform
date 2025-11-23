package com.finx.allocationreallocationservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "customer_code", unique = true, length = 50)
    private String customerId;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "alternate_mobile", length = 15)
    private String alternateMobile;

    @Column(name = "email", length = 100)
    private String emailAddress;

    @Column(name = "alternate_email", length = 100)
    private String alternateEmail;

    @Column(name = "address", length = 255)
    private String addressLine1;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
