package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.ApprovalWorkflow;
import com.finx.configurationsservice.domain.enums.WorkflowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {

    Optional<ApprovalWorkflow> findByWorkflowCode(String workflowCode);

    List<ApprovalWorkflow> findByIsActiveTrue();

    List<ApprovalWorkflow> findByWorkflowType(WorkflowType workflowType);

    Optional<ApprovalWorkflow> findByWorkflowTypeAndIsActiveTrue(WorkflowType workflowType);

    boolean existsByWorkflowCode(String workflowCode);
}
