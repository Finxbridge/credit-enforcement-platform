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
    private final com.finx.strategyengineservice.client.TemplateServiceClient templateServiceClient;
    private final com.finx.strategyengineservice.repository.CaseRepository caseRepository;

    @SuppressWarnings("null")
    @Override
    @CacheEvict(value = { "strategyExecutions", "executionDetails" }, allEntries = true)
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

    @SuppressWarnings("null")
    @Async("strategyExecutionExecutor")
    protected void processStrategyAsync(Long executionId, Long strategyId) {

        try {
            log.info("Processing strategy execution async: {} for strategy: {}", executionId, strategyId);

            // Fetch execution record
            StrategyExecution execution = executionRepository.findById(executionId)
                    .orElseThrow(() -> new BusinessException("Execution not found"));

            // Fetch strategy
            Strategy strategy = strategyRepository.findById(strategyId)
                    .orElseThrow(() -> new BusinessException("Strategy not found"));

            // Fetch strategy rules (filters)
            List<com.finx.strategyengineservice.domain.entity.StrategyRule> rules = strategyRuleRepository
                    .findByStrategyIdOrderByRuleOrderAsc(strategyId);

            // Fetch strategy actions
            List<com.finx.strategyengineservice.domain.entity.StrategyAction> actions = strategyActionRepository
                    .findByStrategyIdOrderByActionOrderAsc(strategyId);

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

        // Build SMS recipient with dynamic variables
        com.finx.strategyengineservice.client.dto.SMSRequest.SmsRecipient recipient =
                com.finx.strategyengineservice.client.dto.SMSRequest.SmsRecipient.builder()
                .mobile(mobile)
                .variables(buildDynamicVariables(caseEntity, action, "SMS"))
                .build();

        // Build SMS request aligned with communication-service format
        com.finx.strategyengineservice.client.dto.SMSRequest request =
                com.finx.strategyengineservice.client.dto.SMSRequest.builder()
                .templateId(action.getTemplateId() != null ? action.getTemplateId().toString() : null)
                .shortUrl("0") // Disable short URL by default
                .recipients(java.util.Collections.singletonList(recipient))
                .caseId(caseEntity.getId())
                .build();

        communicationClient.sendSMS(request);
        log.debug("SMS sent successfully for case: {}", caseEntity.getId());
    }

    /**
     * Build dynamic variables from case data based on template variable mapping
     */
    private java.util.Map<String, Object> buildDynamicVariables(
            com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action,
            String channel) {

        java.util.Map<String, Object> variables = new java.util.HashMap<>();

        // If templateId and variableMapping are set, use dynamic mapping
        if (action.getTemplateId() != null && action.getVariableMapping() != null && !action.getVariableMapping().isEmpty()) {
            try {
                // Fetch template details
                com.finx.strategyengineservice.client.dto.TemplateDetailDTO template =
                    templateServiceClient.getTemplate(action.getTemplateId()).getPayload();

                if (template != null && template.getVariables() != null) {
                    // Map each template variable to case entity value
                    for (com.finx.strategyengineservice.client.dto.TemplateDetailDTO.TemplateVariableDTO templateVar : template.getVariables()) {
                        String variableKey = templateVar.getVariableKey();
                        String entityPath = action.getVariableMapping().get(variableKey);

                        if (entityPath != null) {
                            Object value = extractValueFromCase(caseEntity, entityPath);
                            variables.put(variableKey, value != null ? value : templateVar.getDefaultValue());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching template or building dynamic variables, falling back to defaults", e);
                // Fall back to default hardcoded variables
                return buildDefaultVariables(caseEntity, channel);
            }
        } else {
            // Fall back to default hardcoded variables
            return buildDefaultVariables(caseEntity, channel);
        }

        return variables;
    }

    /**
     * Build default hardcoded variables (backward compatibility)
     */
    private java.util.Map<String, Object> buildDefaultVariables(
            com.finx.strategyengineservice.domain.entity.Case caseEntity, String channel) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();

        if ("SMS".equals(channel)) {
            variables.put("VAR1", caseEntity.getLoan().getPrimaryCustomer().getFullName());
            variables.put("VAR2", caseEntity.getLoan().getLoanAccountNumber());
            variables.put("VAR3", caseEntity.getLoan().getOutstandingAmount() != null
                    ? caseEntity.getLoan().getOutstandingAmount().toString() : "0");
        }

        return variables;
    }

    /**
     * Extract value from case entity using property path (e.g., "loan.primaryCustomer.customerName")
     */
    private Object extractValueFromCase(com.finx.strategyengineservice.domain.entity.Case caseEntity, String propertyPath) {
        try {
            String[] parts = propertyPath.split("\\.");
            Object current = caseEntity;

            for (String part : parts) {
                if (current == null) {
                    return null;
                }

                // Use reflection to get property value
                String methodName = "get" + part.substring(0, 1).toUpperCase() + part.substring(1);
                current = current.getClass().getMethod(methodName).invoke(current);
            }

            return current;
        } catch (Exception e) {
            log.error("Error extracting value from path: {}", propertyPath, e);
            return null;
        }
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

        com.finx.strategyengineservice.client.dto.EmailRequest request = com.finx.strategyengineservice.client.dto.EmailRequest
                .builder()
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

        // Build WhatsApp request aligned with communication-service format
        com.finx.strategyengineservice.client.dto.WhatsAppRequest request =
                com.finx.strategyengineservice.client.dto.WhatsAppRequest.builder()
                .templateId(action.getTemplateId() != null ? action.getTemplateId().toString() : null)
                .to(java.util.Collections.singletonList(mobile))
                .components(buildWhatsAppComponents(caseEntity, action))
                .language(com.finx.strategyengineservice.client.dto.WhatsAppRequest.WhatsAppLanguage.builder()
                        .code("en")
                        .policy("deterministic")
                        .build())
                .caseId(caseEntity.getId())
                .build();

        communicationClient.sendWhatsApp(request);
        log.debug("WhatsApp sent successfully for case: {}", caseEntity.getId());
    }

    /**
     * Build WhatsApp components from case data with dynamic template variables
     */
    private java.util.Map<String, java.util.Map<String, String>> buildWhatsAppComponents(
            com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        java.util.Map<String, java.util.Map<String, String>> components = new java.util.HashMap<>();

        // If templateId and variableMapping are set, use dynamic mapping
        if (action.getTemplateId() != null && action.getVariableMapping() != null && !action.getVariableMapping().isEmpty()) {
            try {
                // Fetch template details
                com.finx.strategyengineservice.client.dto.TemplateDetailDTO template =
                    templateServiceClient.getTemplate(action.getTemplateId()).getPayload();

                if (template != null && template.getVariables() != null) {
                    // Map each template variable to case entity value
                    for (com.finx.strategyengineservice.client.dto.TemplateDetailDTO.TemplateVariableDTO templateVar : template.getVariables()) {
                        String variableKey = templateVar.getVariableKey();
                        String entityPath = action.getVariableMapping().get(variableKey);

                        if (entityPath != null) {
                            Object value = extractValueFromCase(caseEntity, entityPath);
                            String valueStr = value != null ? value.toString() :
                                (templateVar.getDefaultValue() != null ? templateVar.getDefaultValue() : "");

                            java.util.Map<String, String> component = new java.util.HashMap<>();
                            component.put("type", "text");
                            component.put("value", valueStr);
                            components.put(variableKey, component);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching template or building dynamic components, falling back to defaults", e);
                // Fall back to default hardcoded components
                return buildDefaultWhatsAppComponents(caseEntity);
            }
        } else {
            // Fall back to default hardcoded components
            return buildDefaultWhatsAppComponents(caseEntity);
        }

        return components;
    }

    /**
     * Build default WhatsApp components (backward compatibility)
     */
    private java.util.Map<String, java.util.Map<String, String>> buildDefaultWhatsAppComponents(
            com.finx.strategyengineservice.domain.entity.Case caseEntity) {
        java.util.Map<String, java.util.Map<String, String>> components = new java.util.HashMap<>();

        // body_1: Customer name
        java.util.Map<String, String> body1 = new java.util.HashMap<>();
        body1.put("type", "text");
        body1.put("value", caseEntity.getLoan().getPrimaryCustomer().getFullName());
        components.put("body_1", body1);

        // body_2: Loan account number
        java.util.Map<String, String> body2 = new java.util.HashMap<>();
        body2.put("type", "text");
        body2.put("value", caseEntity.getLoan().getLoanAccountNumber());
        components.put("body_2", body2);

        // body_3: Outstanding amount
        java.util.Map<String, String> body3 = new java.util.HashMap<>();
        body3.put("type", "text");
        body3.put("value", caseEntity.getLoan().getOutstandingAmount() != null
                ? caseEntity.getLoan().getOutstandingAmount().toString() : "0");
        components.put("body_3", body3);

        return components;
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
