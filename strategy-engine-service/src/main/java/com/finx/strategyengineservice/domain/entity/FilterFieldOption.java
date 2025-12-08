package com.finx.strategyengineservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store dropdown options for TEXT filters
 * E.g., Language: [Hindi, English, Telugu], State: [Maharashtra, Gujarat]
 */
@Entity
@Table(name = "filter_field_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterFieldOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_field_id", nullable = false)
    private FilterField filterField;

    @Column(name = "option_value", nullable = false, length = 255)
    private String optionValue;  // e.g., "HINDI", "MAHARASHTRA", "500001"

    @Column(name = "option_label", length = 255)
    private String optionLabel;  // Display name, if different from value

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (optionLabel == null || optionLabel.isEmpty()) {
            optionLabel = optionValue;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
