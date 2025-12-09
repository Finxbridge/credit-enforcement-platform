package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.ExecutionDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionDetailDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionInitiatedDTO;
import com.finx.strategyengineservice.domain.dto.ExecutionRunDetailsDTO;
import com.finx.strategyengineservice.domain.entity.CommunicationHistory;
import com.finx.strategyengineservice.domain.entity.Strategy;
import com.finx.strategyengineservice.domain.entity.StrategyExecution;
import com.finx.strategyengineservice.domain.enums.CommunicationChannel;
import com.finx.strategyengineservice.domain.enums.CommunicationStatus;
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
    private final com.finx.strategyengineservice.client.DmsServiceClient dmsServiceClient;
    private final com.finx.strategyengineservice.client.NoticeServiceClient noticeServiceClient;
    private final com.finx.strategyengineservice.repository.CaseRepository caseRepository;
    private final com.finx.strategyengineservice.repository.CommunicationHistoryRepository communicationHistoryRepository;

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
     * Build dynamic variables from case data using centralized template resolution API
     * Uses template-management-service for variable resolution with transformers
     */
    private java.util.Map<String, Object> buildDynamicVariables(
            com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action,
            String channel) {

        // If templateId is set, use centralized template resolution API
        if (action.getTemplateId() != null) {
            try {
                // Call template resolution API
                com.finx.strategyengineservice.client.dto.TemplateResolveRequest resolveRequest =
                    com.finx.strategyengineservice.client.dto.TemplateResolveRequest.builder()
                        .caseId(caseEntity.getId())
                        .additionalContext(null) // Can be extended for additional context
                        .build();

                Long templateId = Long.parseLong(action.getTemplateId());
                com.finx.strategyengineservice.client.dto.TemplateResolveResponse resolveResponse =
                    templateServiceClient.resolveTemplate(templateId, resolveRequest).getPayload();

                if (resolveResponse != null && resolveResponse.getResolvedVariables() != null) {
                    log.debug("Resolved {} variables using centralized template resolution for template ID: {}",
                            resolveResponse.getResolvedVariables().size(), action.getTemplateId());
                    return resolveResponse.getResolvedVariables();
                }

            } catch (Exception e) {
                log.error("Error resolving template variables via API, falling back to defaults", e);
                // Fall back to default hardcoded variables
                return buildDefaultVariables(caseEntity, channel);
            }
        }

        // Fall back to default hardcoded variables
        return buildDefaultVariables(caseEntity, channel);
    }

    /**
     * Build default hardcoded variables (backward compatibility)
     * Used when template resolution API is unavailable or templateId is not set
     */
    private java.util.Map<String, Object> buildDefaultVariables(
            com.finx.strategyengineservice.domain.entity.Case caseEntity, String channel) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();

        if ("SMS".equals(channel)) {
            variables.put("VAR1", caseEntity.getLoan().getPrimaryCustomer().getFullName());
            variables.put("VAR2", caseEntity.getLoan().getLoanAccountNumber());
            variables.put("VAR3", caseEntity.getLoan().getTotalOutstanding() != null
                    ? caseEntity.getLoan().getTotalOutstanding().toString() : "0");
        }

        return variables;
    }

    /**
     * Send Email via communication service using dynamic variable resolution
     */
    private void sendEmail(com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String email = caseEntity.getLoan().getPrimaryCustomer().getEmail();
        if (email == null || email.isEmpty()) {
            throw new BusinessException("Email not available for case: " + caseEntity.getId());
        }

        // Build dynamic variables using centralized template resolution
        Map<String, Object> resolvedVariables = buildDynamicVariables(caseEntity, action, "EMAIL");

        // Convert Map<String, Object> to Map<String, String> for EmailRequest
        Map<String, String> variables = new HashMap<>();
        for (Map.Entry<String, Object> entry : resolvedVariables.entrySet()) {
            variables.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }

        String customerName = caseEntity.getLoan().getPrimaryCustomer().getFullName();

        com.finx.strategyengineservice.client.dto.EmailRequest request = com.finx.strategyengineservice.client.dto.EmailRequest
                .builder()
                .toEmail(email)
                .toName(customerName)
                .templateId(action.getTemplateId() != null ? action.getTemplateId().toString() : null)
                .variables(variables)
                .caseId(caseEntity.getId())
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
     * Build WhatsApp components from case data using centralized template resolution API
     */
    private java.util.Map<String, java.util.Map<String, String>> buildWhatsAppComponents(
            com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        java.util.Map<String, java.util.Map<String, String>> components = new java.util.HashMap<>();

        // If templateId is set, use centralized template resolution API
        if (action.getTemplateId() != null) {
            try {
                // Call template resolution API
                com.finx.strategyengineservice.client.dto.TemplateResolveRequest resolveRequest =
                    com.finx.strategyengineservice.client.dto.TemplateResolveRequest.builder()
                        .caseId(caseEntity.getId())
                        .additionalContext(null)
                        .build();

                Long templateId = Long.parseLong(action.getTemplateId());
                com.finx.strategyengineservice.client.dto.TemplateResolveResponse resolveResponse =
                    templateServiceClient.resolveTemplate(templateId, resolveRequest).getPayload();

                if (resolveResponse != null && resolveResponse.getResolvedVariables() != null) {
                    // Convert resolved variables to WhatsApp component format
                    for (java.util.Map.Entry<String, Object> entry : resolveResponse.getResolvedVariables().entrySet()) {
                        java.util.Map<String, String> component = new java.util.HashMap<>();
                        component.put("type", "text");
                        component.put("value", entry.getValue() != null ? entry.getValue().toString() : "");
                        components.put(entry.getKey(), component);
                    }

                    log.debug("Built {} WhatsApp components using centralized template resolution", components.size());
                    return components;
                }

            } catch (Exception e) {
                log.error("Error resolving template variables via API, falling back to defaults", e);
                // Fall back to default hardcoded components
                return buildDefaultWhatsAppComponents(caseEntity);
            }
        }

        // Fall back to default hardcoded components
        return buildDefaultWhatsAppComponents(caseEntity);
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
        body3.put("value", caseEntity.getLoan().getTotalOutstanding() != null
                ? caseEntity.getLoan().getTotalOutstanding().toString() : "0");
        components.put("body_3", body3);

        return components;
    }

    /**
     * Create notice with document attachment from DMS if available
     */
    private void createNotice(com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {
        createNoticeWithTracking(caseEntity, action, null, null);
    }

    /**
     * Create notice with document attachment and track in communication history
     */
    private void createNoticeWithTracking(com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action,
            Long executionId, Long strategyId) {
        log.info("Creating notice for case: {}", caseEntity.getId());

        String communicationId = "NOTICE_" + System.currentTimeMillis() + "_" + caseEntity.getId();

        if (action.getTemplateId() != null) {
            try {
                Long templateId = Long.parseLong(action.getTemplateId());

                // Call template resolution API to get resolved content and document info
                com.finx.strategyengineservice.client.dto.TemplateResolveRequest resolveRequest =
                    com.finx.strategyengineservice.client.dto.TemplateResolveRequest.builder()
                        .caseId(caseEntity.getId())
                        .additionalContext(null)
                        .build();

                com.finx.strategyengineservice.client.dto.TemplateResolveResponse resolveResponse =
                    templateServiceClient.resolveTemplate(templateId, resolveRequest).getPayload();

                if (resolveResponse != null) {
                    log.info("Template resolved for notice - templateId: {}, hasDocument: {}",
                            templateId, resolveResponse.getHasDocument());

                    // Build create notice request
                    com.finx.strategyengineservice.client.dto.CreateNoticeRequest noticeRequest =
                        com.finx.strategyengineservice.client.dto.CreateNoticeRequest.builder()
                            .caseId(caseEntity.getId())
                            .loanAccountNumber(caseEntity.getLoan() != null ? caseEntity.getLoan().getLoanAccountNumber() : null)
                            .customerName(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null
                                ? caseEntity.getLoan().getPrimaryCustomer().getFullName() : null)
                            .noticeType("DEMAND")  // Default, can be configured in action
                            .templateId(templateId)
                            .renderedContent(resolveResponse.getRenderedContent())
                            .build();

                    // Set recipient info from customer
                    if (caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null) {
                        var customer = caseEntity.getLoan().getPrimaryCustomer();
                        noticeRequest.setRecipientName(customer.getFullName());
                        noticeRequest.setRecipientAddress(customer.getAddress());
                        noticeRequest.setRecipientCity(customer.getCity());
                        noticeRequest.setRecipientState(customer.getState());
                        noticeRequest.setRecipientPincode(customer.getPincode());
                    }

                    // Add document info if template has document attachment
                    if (Boolean.TRUE.equals(resolveResponse.getHasDocument()) &&
                            resolveResponse.getDmsDocumentId() != null) {

                        noticeRequest.setDmsDocumentId(resolveResponse.getDmsDocumentId());
                        noticeRequest.setOriginalDocumentUrl(resolveResponse.getOriginalDocumentUrl());
                        noticeRequest.setProcessedDocumentUrl(resolveResponse.getProcessedDocumentUrl());
                        noticeRequest.setDocumentType(resolveResponse.getDocumentType());
                        noticeRequest.setDocumentOriginalName(resolveResponse.getDocumentOriginalName());

                        log.info("Notice document attached - DMS ID: {}, Type: {}, Name: {}",
                                resolveResponse.getDmsDocumentId(),
                                resolveResponse.getDocumentType(),
                                resolveResponse.getDocumentOriginalName());
                    }

                    // Create notice via notice-management-service
                    var noticeResponse = noticeServiceClient.createNotice(noticeRequest);

                    if (noticeResponse != null && noticeResponse.getPayload() != null) {
                        var noticeDTO = noticeResponse.getPayload();
                        log.info("Notice created successfully - Notice Number: {}, Status: {}",
                                noticeDTO.getNoticeNumber(), noticeDTO.getNoticeStatus());

                        // Save communication history
                        CommunicationHistory history = CommunicationHistory.builder()
                                .communicationId(communicationId)
                                .caseId(caseEntity.getId())
                                .executionId(executionId)
                                .strategyId(strategyId)
                                .actionId(action.getId())
                                .channel(CommunicationChannel.NOTICE)
                                .templateId(templateId)
                                .templateCode(resolveResponse.getTemplateCode())
                                .recipientName(noticeRequest.getRecipientName())
                                .recipientAddress(noticeRequest.getRecipientAddress())
                                .content(resolveResponse.getRenderedContent())
                                .hasDocument(Boolean.TRUE.equals(resolveResponse.getHasDocument()))
                                .dmsDocumentId(resolveResponse.getDmsDocumentId())
                                .originalDocumentUrl(resolveResponse.getOriginalDocumentUrl())
                                .processedDocumentUrl(resolveResponse.getProcessedDocumentUrl())
                                .documentType(resolveResponse.getDocumentType())
                                .documentOriginalName(resolveResponse.getDocumentOriginalName())
                                .status(CommunicationStatus.GENERATED)
                                .noticeId(noticeDTO.getId())
                                .noticeNumber(noticeDTO.getNoticeNumber())
                                .sentAt(LocalDateTime.now())
                                .build();

                        communicationHistoryRepository.save(history);
                        log.info("Communication history saved for notice: {}", noticeDTO.getNoticeNumber());
                    }
                }

            } catch (Exception e) {
                log.error("Error creating notice for case {}: {}", caseEntity.getId(), e.getMessage());

                // Save failed communication history
                CommunicationHistory failedHistory = CommunicationHistory.builder()
                        .communicationId(communicationId)
                        .caseId(caseEntity.getId())
                        .executionId(executionId)
                        .strategyId(strategyId)
                        .actionId(action.getId())
                        .channel(CommunicationChannel.NOTICE)
                        .templateId(action.getTemplateId() != null ? Long.parseLong(action.getTemplateId()) : null)
                        .status(CommunicationStatus.FAILED)
                        .failureReason(e.getMessage())
                        .failedAt(LocalDateTime.now())
                        .build();

                communicationHistoryRepository.save(failedHistory);
            }
        }
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
