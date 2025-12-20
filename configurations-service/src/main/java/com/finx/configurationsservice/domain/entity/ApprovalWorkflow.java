package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.WorkflowType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "approval_workflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_code", unique = true, nullable = false, length = 50)
    private String workflowCode;

    @Column(name = "workflow_name", nullable = false, length = 200)
    private String workflowName;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false, length = 50)
    private WorkflowType workflowType;

    @Type(JsonType.class)
    @Column(name = "approval_levels", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> approvalLevels;

    @Column(name = "escalation_enabled")
    private Boolean escalationEnabled;

    @Column(name = "escalation_hours")
    private Integer escalationHours;

    @Column(name = "auto_approve_enabled")
    private Boolean autoApproveEnabled;

    @Type(JsonType.class)
    @Column(name = "auto_approve_criteria", columnDefinition = "jsonb")
    private Map<String, Object> autoApproveCriteria;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (escalationEnabled == null) {
            escalationEnabled = true;
        }
        if (escalationHours == null) {
            escalationHours = 24;
        }
        if (autoApproveEnabled == null) {
            autoApproveEnabled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
