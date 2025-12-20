package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.WorkflowType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalWorkflowRequest {

    @NotBlank(message = "Workflow code is required")
    @Size(max = 50, message = "Workflow code must be at most 50 characters")
    private String workflowCode;

    @NotBlank(message = "Workflow name is required")
    @Size(max = 200, message = "Workflow name must be at most 200 characters")
    private String workflowName;

    @NotNull(message = "Workflow type is required")
    private WorkflowType workflowType;

    @NotNull(message = "Approval levels are required")
    private List<Map<String, Object>> approvalLevels;

    private Boolean escalationEnabled;
    private Integer escalationHours;
    private Boolean autoApproveEnabled;
    private Map<String, Object> autoApproveCriteria;
}
