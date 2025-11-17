package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.ExecutionDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionDetailDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionInitiatedDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionRunDetailsDTO;
import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.entity.StrategyExecution;
import com.finx.strategyengineservice.domain.enums.ExecutionStatus;
import com.finx.strategyengineservice.domain.enums.ExecutionType;
import com.finx.strategyengineservice.exception.BusinessException;
import com.finx.strategyengineservice.repository.StrategyExecutionRepository;
import com.finx.strategyengineservice.repository.StrategyRepository;
import com.finx.strategyengineservice.service.StrategyExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StrategyExecutionServiceImpl implements StrategyExecutionService {

    private final StrategyExecutionRepository executionRepository;
    private final StrategyRepository strategyRepository;
    private final com.finx.strategyengineservice.service.CaseFilterService caseFilterService;
    private final com.finx.strategyengineservice.repository.StrategyRuleRepository strategyRuleRepository;
    private final com.finx.strategyengineservice.repository.StrategyActionRepository strategyActionRepository;
    private final com.finx.strategyengineservice.client.CommunicationServiceClient communicationClient;
    private final com.finx.strategyengineservice.repository.CaseRepository caseRepository;

    @Override
    @CacheEvict(value = {"strategyExecutions", "executionDetails"}, allEntries = true)
    public ExecutionInitiatedDTO executeStrategy(Long strategyId) {
        log.info("Initiating execution for strategy ID: {}", strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException("Strategy not found with ID: " + strategyId));

        // Create execution record
        StrategyExecution execution = StrategyExecution.builder()
                .executionId("exec_" + System.currentTimeMillis())
                .strategyId(strategy.getId())
                .strategyName(strategy.getStrategyName())
                .executionType(ExecutionType.MANUAL)
                .executionStatus(ExecutionStatus.PROCESSING)
                .startedAt(LocalDateTime.now())
                .build();

        StrategyExecution savedExecution = executionRepository.save(execution);
        log.info("Execution initiated with ID: {}", savedExecution.getExecutionId());

        // Trigger async processing
        processStrategyAsync(savedExecution.getId(), strategy.getId());

        return ExecutionInitiatedDTO.builder()
                .executionId(savedExecution.getExecutionId())
                .strategyId(strategyId)
                .status(ExecutionStatus.PROCESSING.name())
                .build();
    }

    @Async("strategyExecutionExecutor")
    protected void processStrategyAsync(Long executionId, Long strategyId) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("Processing strategy execution async: {} for strategy: {}", executionId, strategyId);

            // Fetch execution record
            StrategyExecution execution = executionRepository.findById(executionId)
                    .orElseThrow(() -> new BusinessException("Execution not found"));

            // Fetch strategy
            Strategy strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new BusinessException("Strategy not found"));

            // Fetch strategy rules (filters)
            List<com.finx.strategyengineservice.domain.entity.StrategyRule> rules =
                strategyRuleRepository.findByStrategyIdOrderByRuleOrderAsc(strategyId);

            // Fetch strategy actions
            List<com.finx.strategyengineservice.domain.entity.StrategyAction> actions =
                strategyActionRepository.findByStrategyIdOrderByActionOrderAsc(strategyId);

            if (actions.isEmpty()) {
                throw new BusinessException("No actions defined for strategy: " + strategyId);
            }

            log.info("Found {} rules and {} actions for strategy {}", rules.size(), actions.size(), strategyId);

            // STEP 1: Filter cases based on rules
            List<com.finx.strategyengineservice.domain.entity.Case> matchedCases;
            if (rules.isEmpty()) {
                // No rules = get all allocated cases
                matchedCases = caseRepository.findAllAllocatedCasesWithLoan();
                log.info("No rules defined, fetched all allocated cases: {}", matchedCases.size());
            } else {
                // Apply filters
                matchedCases = caseFilterService.filterCasesByRules(rules);
                log.info("Filtered {} cases matching strategy rules", matchedCases.size());
            }

            execution.setTotalRecordsEvaluated(matchedCases.size());
            execution.setRecordsMatched(matchedCases.size());

            if (matchedCases.isEmpty()) {
                log.info("No cases matched the strategy rules. Completing execution.");
                execution.setExecutionStatus(ExecutionStatus.COMPLETED);
                execution.setTotalCasesProcessed(0);
                execution.setSuccessfulActions(0);
                execution.setFailedActions(0);
                execution.setCompletedAt(LocalDateTime.now());
                executionRepository.save(execution);

                updateStrategyStats(strategy, 0, 0);
                return;
            }

            // STEP 2: Execute actions on matched cases
            int successCount = 0;
            int failureCount = 0;
            List<Map<String, Object>> executionLog = new ArrayList<>();

            for (com.finx.strategyengineservice.domain.entity.Case caseEntity : matchedCases) {
                for (com.finx.strategyengineservice.domain.entity.StrategyAction action : actions) {
                    try {
                        executeAction(caseEntity, action);
                        successCount++;
                    } catch (Exception e) {
                        failureCount++;

                        // Log error
                        Map<String, Object> errorLog = new HashMap<>();
                        errorLog.put("caseId", caseEntity.getId());
                        errorLog.put("caseNumber", caseEntity.getCaseNumber());
                        errorLog.put("actionType", action.getActionType().name());
                        errorLog.put("error", e.getMessage());
                        errorLog.put("timestamp", LocalDateTime.now().toString());
                        executionLog.add(errorLog);

                        log.error("Failed to execute action {} for case {}: {}",
                            action.getActionType(), caseEntity.getId(), e.getMessage());
                    }
                }
            }

            // STEP 3: Update execution record
            execution.setExecutionStatus(ExecutionStatus.COMPLETED);
            execution.setTotalCasesProcessed(matchedCases.size());
            execution.setRecordsProcessed(matchedCases.size());
            execution.setSuccessfulActions(successCount);
            execution.setFailedActions(failureCount);
            execution.setRecordsFailed(failureCount);
            execution.setCompletedAt(LocalDateTime.now());

            if (!executionLog.isEmpty()) {
                execution.setExecutionLog(executionLog);
            }

            executionRepository.save(execution);

            // STEP 4: Update strategy statistics
            updateStrategyStats(strategy, successCount, failureCount);

            log.info("Strategy execution completed: {}. Processed: {}, Success: {}, Failed: {}",
                execution.getExecutionId(), matchedCases.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("Fatal error processing strategy execution: {}", executionId, e);

            StrategyExecution execution = executionRepository.findById(executionId).orElse(null);
            if (execution != null) {
                execution.setExecutionStatus(ExecutionStatus.FAILED);
                execution.setErrorMessage("Execution failed: " + e.getMessage());
                execution.setCompletedAt(LocalDateTime.now());
                executionRepository.save(execution);
            }
        }
    }

    /**
     * Execute a single action on a case
     */
    private void executeAction(com.finx.strategyengineservice.domain.entity.Case caseEntity,
                                com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        log.debug("Executing action {} for case {}", action.getActionType(), caseEntity.getId());

        switch (action.getActionType()) {
            case SEND_SMS:
                sendSMS(caseEntity, action);
                break;

            case SEND_EMAIL:
                sendEmail(caseEntity, action);
                break;

            case SEND_WHATSAPP:
                sendWhatsApp(caseEntity, action);
                break;

            case CREATE_NOTICE:
                createNotice(caseEntity, action);
                break;

            case SCHEDULE_CALL:
                scheduleCall(caseEntity, action);
                break;

            default:
                log.warn("Unsupported action type: {}", action.getActionType());
        }
    }

    /**
     * Send SMS via communication service
     */
    private void sendSMS(com.finx.strategyengineservice.domain.entity.Case caseEntity,
                         com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String mobile = caseEntity.getLoan().getPrimaryCustomer().getMobileNumber();
        if (mobile == null || mobile.isEmpty()) {
            throw new BusinessException("Mobile number not available for case: " + caseEntity.getId());
        }

        com.finx.strategyengineservice.client.dto.SMSRequest request =
            com.finx.strategyengineservice.client.dto.SMSRequest.builder()
                .mobile(mobile)
                .message("Payment reminder for loan: " + caseEntity.getLoan().getLoanAccountNumber())
                .templateId(action.getTemplateId() != null ? action.getTemplateId().toString() : null)
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .build();

        communicationClient.sendSMS(request);
        log.debug("SMS sent successfully for case: {}", caseEntity.getId());
    }

    /**
     * Send Email via communication service
     */
    private void sendEmail(com.finx.strategyengineservice.domain.entity.Case caseEntity,
                           com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String email = caseEntity.getLoan().getPrimaryCustomer().getEmailAddress();
        if (email == null || email.isEmpty()) {
            throw new BusinessException("Email not available for case: " + caseEntity.getId());
        }

        com.finx.strategyengineservice.client.dto.EmailRequest request =
            com.finx.strategyengineservice.client.dto.EmailRequest.builder()
                .email(email)
                .subject("Payment Reminder")
                .body("Payment reminder for loan: " + caseEntity.getLoan().getLoanAccountNumber())
                .templateId(action.getTemplateId() != null ? action.getTemplateId().toString() : null)
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .build();

        communicationClient.sendEmail(request);
        log.debug("Email sent successfully for case: {}", caseEntity.getId());
    }

    /**
     * Send WhatsApp via communication service
     */
    private void sendWhatsApp(com.finx.strategyengineservice.domain.entity.Case caseEntity,
                              com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String mobile = caseEntity.getLoan().getPrimaryCustomer().getMobileNumber();
        if (mobile == null || mobile.isEmpty()) {
            throw new BusinessException("Mobile number not available for case: " + caseEntity.getId());
        }

        com.finx.strategyengineservice.client.dto.WhatsAppRequest request =
            com.finx.strategyengineservice.client.dto.WhatsAppRequest.builder()
                .mobile(mobile)
                .message("Payment reminder for loan: " + caseEntity.getLoan().getLoanAccountNumber())
                .templateId(action.getTemplateId() != null ? action.getTemplateId().toString() : null)
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .build();

        communicationClient.sendWhatsApp(request);
        log.debug("WhatsApp sent successfully for case: {}", caseEntity.getId());
    }

    /**
     * Create notice (placeholder - implement based on requirements)
     */
    private void createNotice(com.finx.strategyengineservice.domain.entity.Case caseEntity,
                              com.finx.strategyengineservice.domain.entity.StrategyAction action) {
        log.info("Creating notice for case: {} (Not yet implemented)", caseEntity.getId());
        // TODO: Implement notice creation logic
    }

    /**
     * Schedule call (placeholder - implement based on requirements)
     */
    private void scheduleCall(com.finx.strategyengineservice.domain.entity.Case caseEntity,
                              com.finx.strategyengineservice.domain.entity.StrategyAction action) {
        log.info("Scheduling call for case: {} (Not yet implemented)", caseEntity.getId());
        // TODO: Implement call scheduling logic (IVR integration)
    }

    /**
     * Update strategy statistics
     */
    private void updateStrategyStats(Strategy strategy, int successCount, int failureCount) {
        strategy.setLastRunAt(LocalDateTime.now());
        strategy.setSuccessCount(strategy.getSuccessCount() + successCount);
        strategy.setFailureCount(strategy.getFailureCount() + failureCount);
        strategyRepository.save(strategy);
    }

    @Override
    @Cacheable(value = "strategyExecutions", key = "'all'")
    @Transactional(readOnly = true)
    public List<ExecutionDTO> getAllExecutions() {
        log.info("Fetching all strategy executions");
        List<StrategyExecution> executions = executionRepository.findAllByOrderByStartedAtDesc(null).getContent();

        return executions.stream()
                .map(this::convertToExecutionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "executionDetails", key = "#executionId")
    @Transactional(readOnly = true)
    public ExecutionDetailDTO getExecutionDetails(String executionId) {
        log.info("Fetching execution details for ID: {}", executionId);
        StrategyExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new BusinessException("Execution not found with ID: " + executionId));

        return convertToExecutionDetailDTO(execution);
    }

    @Override
    @Cacheable(value = "executionDetails", key = "'details_' + #executionId")
    @Transactional(readOnly = true)
    public ExecutionRunDetailsDTO getExecutionRunDetails(String executionId) {
        log.info("Fetching execution run details for ID: {}", executionId);
        StrategyExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new BusinessException("Execution not found with ID: " + executionId));

        return convertToExecutionRunDetailsDTO(execution);
    }

    // Conversion methods
    private ExecutionDTO convertToExecutionDTO(StrategyExecution execution) {
        return ExecutionDTO.builder()
                .executionId(execution.getExecutionId())
                .strategyId(execution.getStrategyId())
                .strategyName(execution.getStrategyName())
                .status(execution.getExecutionStatus().name())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .totalCasesProcessed(execution.getTotalCasesProcessed())
                .successfulActions(execution.getSuccessfulActions())
                .failedActions(execution.getFailedActions())
                .build();
    }

    private ExecutionDetailDTO convertToExecutionDetailDTO(StrategyExecution execution) {
        return ExecutionDetailDTO.builder()
                .executionId(execution.getExecutionId())
                .strategyId(execution.getStrategyId())
                .strategyName(execution.getStrategyName())
                .status(execution.getExecutionStatus().name())
                .totalCasesProcessed(execution.getTotalCasesProcessed())
                .successfulActions(execution.getSuccessfulActions())
                .failedActions(execution.getFailedActions())
                .errors(execution.getExecutionLog())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .build();
    }

    private ExecutionRunDetailsDTO convertToExecutionRunDetailsDTO(StrategyExecution execution) {
        // Convert execution log to details format as per API spec
        List<Map<String, Object>> details = new ArrayList<>();

        if (execution.getExecutionLog() != null) {
            details.addAll(execution.getExecutionLog());
        }

        return ExecutionRunDetailsDTO.builder()
                .executionId(execution.getExecutionId())
                .details(details)
                .build();
    }
}
