package com.finx.strategyengineservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity to store dynamic filter field metadata
 * Allows admin to add/edit/deactivate filters without code changes
 */
@Entity
@Table(name = "filter_fields")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_code", unique = true, nullable = false, length = 100)
    private String fieldCode;  // e.g., "DPD", "OVERDUE_AMOUNT", "LANGUAGE"

    @Column(name = "field_key", nullable = false, length = 100)
    private String fieldKey;  // e.g., "dpd", "odVal", "language" - used in case data mapping

    @Column(name = "field_type", nullable = false, length = 20)
    private String fieldType;  // TEXT, NUMERIC, DATE

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;  // e.g., "DPD", "Overdue Amount"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_attribute")
    @Builder.Default
    private Boolean isAttribute = false;  // For TEXT filters with predefined options

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    // For TEXT filters with predefined options (like Language, State, City)
    @OneToMany(mappedBy = "filterField", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FilterFieldOption> options = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addOption(FilterFieldOption option) {
        options.add(option);
        option.setFilterField(this);
    }

    public void removeOption(FilterFieldOption option) {
        options.remove(option);
        option.setFilterField(null);
    }
}
