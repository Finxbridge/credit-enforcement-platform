package com.finx.templatemanagementservice.domain.entity;

import com.finx.templatemanagementservice.domain.enums.DataType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Template Variable entity for dynamic placeholders
 */
@Entity
@Table(name = "template_variables", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "variable_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "template")
@ToString(exclude = "template")
public class TemplateVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "variable_name", nullable = false, length = 255)
    private String variableName; // VAR1, VAR2, body_1, header_1, etc.

    @Column(name = "variable_key", nullable = false, length = 255)
    private String variableKey; // customer_name, loan_account, outstanding_amount

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20)
    @Builder.Default
    private DataType dataType = DataType.TEXT;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
