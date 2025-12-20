package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.dto.ApprovalWorkflowDTO;
import com.finx.configurationsservice.domain.dto.CreateApprovalWorkflowRequest;
import com.finx.configurationsservice.domain.entity.ApprovalWorkflow;
import com.finx.configurationsservice.domain.enums.WorkflowType;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.ApprovalWorkflowRepository;
import com.finx.configurationsservice.service.ApprovalWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {

    private final ApprovalWorkflowRepository workflowRepository;
    private final ConfigurationsMapper mapper;

    @Override
    @CacheEvict(value = "approvalWorkflows", allEntries = true)
    public ApprovalWorkflowDTO createWorkflow(CreateApprovalWorkflowRequest request, Long createdBy) {
        if (workflowRepository.existsByWorkflowCode(request.getWorkflowCode())) {
            throw new BusinessException("Approval workflow with code " + request.getWorkflowCode() + " already exists");
        }

        ApprovalWorkflow workflow = ApprovalWorkflow.builder()
                .workflowCode(request.getWorkflowCode())
                .workflowName(request.getWorkflowName())
                .workflowType(request.getWorkflowType())
                .approvalLevels(request.getApprovalLevels())
                .escalationEnabled(request.getEscalationEnabled())
                .escalationHours(request.getEscalationHours())
                .autoApproveEnabled(request.getAutoApproveEnabled())
                .autoApproveCriteria(request.getAutoApproveCriteria())
                .createdBy(createdBy)
                .isActive(true)
                .build();

        workflow = workflowRepository.save(workflow);
        log.info("Created approval workflow: {}", workflow.getWorkflowCode());
        return mapper.toDto(workflow);
    }

    @Override
    @Cacheable(value = "approvalWorkflows", key = "#id")
    @Transactional(readOnly = true)
    public ApprovalWorkflowDTO getWorkflowById(Long id) {
        ApprovalWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));
        return mapper.toDto(workflow);
    }

    @Override
    @Cacheable(value = "approvalWorkflows", key = "'code-' + #workflowCode")
    @Transactional(readOnly = true)
    public ApprovalWorkflowDTO getWorkflowByCode(String workflowCode) {
        ApprovalWorkflow workflow = workflowRepository.findByWorkflowCode(workflowCode)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with code: " + workflowCode));
        return mapper.toDto(workflow);
    }

    @Override
    @Cacheable(value = "approvalWorkflows", key = "'activeType-' + #type")
    @Transactional(readOnly = true)
    public ApprovalWorkflowDTO getActiveWorkflowByType(WorkflowType type) {
        ApprovalWorkflow workflow = workflowRepository.findByWorkflowTypeAndIsActiveTrue(type)
                .orElseThrow(() -> new ResourceNotFoundException("No active approval workflow found for type: " + type));
        return mapper.toDto(workflow);
    }

    @Override
    @Cacheable(value = "approvalWorkflows", key = "'active'")
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getActiveWorkflows() {
        return mapper.toApprovalWorkflowDtoList(workflowRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getWorkflowsByType(WorkflowType type) {
        return mapper.toApprovalWorkflowDtoList(workflowRepository.findByWorkflowType(type));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApprovalWorkflowDTO> getAllWorkflows(Pageable pageable) {
        return workflowRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @CacheEvict(value = "approvalWorkflows", allEntries = true)
    public ApprovalWorkflowDTO updateWorkflow(Long id, CreateApprovalWorkflowRequest request) {
        ApprovalWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));

        if (!workflow.getWorkflowCode().equals(request.getWorkflowCode()) &&
                workflowRepository.existsByWorkflowCode(request.getWorkflowCode())) {
            throw new BusinessException("Approval workflow with code " + request.getWorkflowCode() + " already exists");
        }

        workflow.setWorkflowCode(request.getWorkflowCode());
        workflow.setWorkflowName(request.getWorkflowName());
        workflow.setWorkflowType(request.getWorkflowType());
        workflow.setApprovalLevels(request.getApprovalLevels());
        if (request.getEscalationEnabled() != null) workflow.setEscalationEnabled(request.getEscalationEnabled());
        if (request.getEscalationHours() != null) workflow.setEscalationHours(request.getEscalationHours());
        if (request.getAutoApproveEnabled() != null) workflow.setAutoApproveEnabled(request.getAutoApproveEnabled());
        if (request.getAutoApproveCriteria() != null) workflow.setAutoApproveCriteria(request.getAutoApproveCriteria());

        workflow = workflowRepository.save(workflow);
        log.info("Updated approval workflow: {}", workflow.getWorkflowCode());
        return mapper.toDto(workflow);
    }

    @Override
    @CacheEvict(value = "approvalWorkflows", allEntries = true)
    public ApprovalWorkflowDTO activateWorkflow(Long id) {
        ApprovalWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));

        // Deactivate existing active workflow of the same type
        final var workflowType = workflow.getWorkflowType();
        workflowRepository.findByWorkflowTypeAndIsActiveTrue(workflowType)
                .ifPresent(existingActive -> {
                    if (!existingActive.getId().equals(id)) {
                        existingActive.setIsActive(false);
                        workflowRepository.save(existingActive);
                        log.info("Deactivated previous workflow: {} for type: {}",
                                existingActive.getWorkflowCode(), workflowType);
                    }
                });

        workflow.setIsActive(true);
        workflow = workflowRepository.save(workflow);
        log.info("Activated approval workflow: {}", workflow.getWorkflowCode());
        return mapper.toDto(workflow);
    }

    @Override
    @CacheEvict(value = "approvalWorkflows", allEntries = true)
    public ApprovalWorkflowDTO deactivateWorkflow(Long id) {
        ApprovalWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));

        workflow.setIsActive(false);
        workflow = workflowRepository.save(workflow);
        log.info("Deactivated approval workflow: {}", workflow.getWorkflowCode());
        return mapper.toDto(workflow);
    }

    @Override
    @CacheEvict(value = "approvalWorkflows", allEntries = true)
    public void deleteWorkflow(Long id) {
        ApprovalWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));

        if (workflow.getIsActive()) {
            throw new BusinessException("Cannot delete active workflow. Please deactivate it first.");
        }

        workflowRepository.delete(workflow);
        log.info("Deleted approval workflow: {}", workflow.getWorkflowCode());
    }

    @Override
    @Transactional(readOnly = true)
    public int getApprovalLevelCount(Long id) {
        ApprovalWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));

        return workflow.getApprovalLevels() != null ? workflow.getApprovalLevels().size() : 0;
    }
}
