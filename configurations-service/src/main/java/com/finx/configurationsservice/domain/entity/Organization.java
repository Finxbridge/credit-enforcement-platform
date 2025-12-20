package com.finx.configurationsservice.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_code", unique = true, nullable = false, length = 50)
    private String orgCode;

    @Column(name = "org_name", nullable = false, length = 255)
    private String orgName;

    @Column(name = "legal_name", length = 255)
    private String legalName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "default_currency", length = 10)
    private String defaultCurrency;

    @Column(name = "default_language", length = 10)
    private String defaultLanguage;

    @Column(name = "default_timezone", length = 50)
    private String defaultTimezone;

    @Column(name = "date_format", length = 20)
    private String dateFormat;

    @Column(name = "license_type", length = 50)
    private String licenseType;

    @Column(name = "license_valid_until")
    private LocalDate licenseValidUntil;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_cases")
    private Integer maxCases;

    @Type(JsonType.class)
    @Column(name = "enabled_features", columnDefinition = "jsonb")
    private Map<String, Boolean> enabledFeatures;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (defaultCurrency == null) {
            defaultCurrency = "INR";
        }
        if (defaultLanguage == null) {
            defaultLanguage = "en";
        }
        if (defaultTimezone == null) {
            defaultTimezone = "Asia/Kolkata";
        }
        if (dateFormat == null) {
            dateFormat = "DD/MM/YYYY";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
