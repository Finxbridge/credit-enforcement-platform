package com.finx.templatemanagementservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Variable Definition Entity
 * Centralized registry for template variables with entity path mappings
 */
@Entity
@Table(name = "variable_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variable_key", unique = true, nullable = false, length = 255)
    private String variableKey;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "entity_path", nullable = false, length = 500)
    private String entityPath;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType; // TEXT, NUMBER, DATE, CURRENCY, BOOLEAN, EMAIL, PHONE

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "transformer", length = 50)
    private String transformer; // DATE_DDMMYYYY, CURRENCY_INR, UPPERCASE, CAPITALIZE, LOWERCASE

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category; // CUSTOMER, LOAN, PAYMENT, CASE, DATES, COMPANY

    @Column(name = "example_value", length = 200)
    private String exampleValue;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

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
