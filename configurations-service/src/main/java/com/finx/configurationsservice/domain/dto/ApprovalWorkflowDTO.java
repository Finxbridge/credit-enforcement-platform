package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.WorkflowType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowDTO {
    private Long id;
    private String workflowCode;
    private String workflowName;
    private WorkflowType workflowType;
    private List<Map<String, Object>> approvalLevels;
    private Boolean escalationEnabled;
    private Integer escalationHours;
    private Boolean autoApproveEnabled;
    private Map<String, Object> autoApproveCriteria;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
