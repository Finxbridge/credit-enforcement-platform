package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.ApprovalWorkflowDTO;
import com.finx.configurationsservice.domain.dto.CreateApprovalWorkflowRequest;
import com.finx.configurationsservice.domain.enums.WorkflowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApprovalWorkflowService {

    ApprovalWorkflowDTO createWorkflow(CreateApprovalWorkflowRequest request, Long createdBy);

    ApprovalWorkflowDTO getWorkflowById(Long id);

    ApprovalWorkflowDTO getWorkflowByCode(String workflowCode);

    ApprovalWorkflowDTO getActiveWorkflowByType(WorkflowType type);

    List<ApprovalWorkflowDTO> getActiveWorkflows();

    List<ApprovalWorkflowDTO> getWorkflowsByType(WorkflowType type);

    Page<ApprovalWorkflowDTO> getAllWorkflows(Pageable pageable);

    ApprovalWorkflowDTO updateWorkflow(Long id, CreateApprovalWorkflowRequest request);

    ApprovalWorkflowDTO activateWorkflow(Long id);

    ApprovalWorkflowDTO deactivateWorkflow(Long id);

    void deleteWorkflow(Long id);

    int getApprovalLevelCount(Long id);
}
