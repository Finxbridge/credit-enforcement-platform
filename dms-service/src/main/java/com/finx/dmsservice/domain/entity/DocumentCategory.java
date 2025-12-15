package com.finx.dmsservice.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "document_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", unique = true, nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(name = "allowed_file_types", columnDefinition = "jsonb")
    private List<String> allowedFileTypes;

    @Column(name = "max_file_size_mb")
    private Integer maxFileSizeMb;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (maxFileSizeMb == null) {
            maxFileSizeMb = 10;
        }
    }
}
