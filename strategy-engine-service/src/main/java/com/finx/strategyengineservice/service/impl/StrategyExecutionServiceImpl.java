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
     *
     * IMPORTANT: If template resolution fails, we DO NOT send the message.
     * Messages should only be sent with properly resolved template data.
     */
    private void sendSMS(com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String mobile = caseEntity.getLoan().getPrimaryCustomer().getMobileNumber();
        if (mobile == null || mobile.isEmpty()) {
            throw new BusinessException("Mobile number not available for case: " + caseEntity.getId());
        }

        // Add India country code (91) if not present - MSG91 requires country code
        mobile = formatMobileWithCountryCode(mobile);

        // Template ID is required for SMS
        if (action.getTemplateId() == null || action.getTemplateId().isEmpty()) {
            throw new BusinessException("Template ID is required for SMS action on case: " + caseEntity.getId());
        }

        // Resolve template - this MUST succeed for message to be sent
        com.finx.strategyengineservice.client.dto.TemplateResolveRequest resolveRequest =
            com.finx.strategyengineservice.client.dto.TemplateResolveRequest.builder()
                .caseId(caseEntity.getId())
                .additionalContext(null)
                .build();

        Long templateId = Long.parseLong(action.getTemplateId());

        com.finx.strategyengineservice.domain.dto.CommonResponse<com.finx.strategyengineservice.client.dto.TemplateResolveResponse> response;
        try {
            response = templateServiceClient.resolveTemplate(templateId, resolveRequest);
        } catch (Exception e) {
            log.error("Failed to resolve template {} for SMS case {}: {}", templateId, caseEntity.getId(), e.getMessage());
            throw new BusinessException("Template resolution failed for SMS case " + caseEntity.getId() + ": " + e.getMessage());
        }

        if (response == null || response.getPayload() == null) {
            throw new BusinessException("Template resolution returned empty response for SMS case: " + caseEntity.getId());
        }

        com.finx.strategyengineservice.client.dto.TemplateResolveResponse resolveResponse = response.getPayload();

        // Validate providerTemplateId - MSG91 template_id
        String msg91TemplateId = resolveResponse.getProviderTemplateId();
        if (msg91TemplateId == null || msg91TemplateId.isEmpty()) {
            throw new BusinessException("MSG91 provider template ID is missing in resolution response for SMS case: " + caseEntity.getId() +
                ". Make sure the template was synced with MSG91 and has a valid provider_template_id.");
        }

        // Get resolved variables
        java.util.Map<String, Object> resolvedVariables = resolveResponse.getResolvedVariables();
        if (resolvedVariables == null || resolvedVariables.isEmpty()) {
            log.warn("No resolved variables found for SMS template {} case {}", templateId, caseEntity.getId());
            resolvedVariables = new java.util.HashMap<>();
        }

        log.info("Resolved {} variables for SMS template {} case {}", resolvedVariables.size(), templateId, caseEntity.getId());

        // Build SMS recipient with resolved variables
        com.finx.strategyengineservice.client.dto.SMSRequest.SmsRecipient recipient =
                com.finx.strategyengineservice.client.dto.SMSRequest.SmsRecipient.builder()
                .mobile(mobile)
                .variables(resolvedVariables)
                .build();

        // Build SMS request - use MSG91 template_id
        com.finx.strategyengineservice.client.dto.SMSRequest request =
                com.finx.strategyengineservice.client.dto.SMSRequest.builder()
                .templateId(msg91TemplateId) // MSG91 template_id returned during template creation
                .shortUrl("0")
                .recipients(java.util.Collections.singletonList(recipient))
                .caseId(caseEntity.getId())
                .build();

        log.info("Sending SMS for case: {} using MSG91 template ID: {}", caseEntity.getId(), msg91TemplateId);

        communicationClient.sendSMS(request);
        log.info("SMS sent successfully for case: {} using MSG91 template ID: {}", caseEntity.getId(), msg91TemplateId);
    }

    /**
     * Send Email via communication service
     *
     * IMPORTANT: If template resolution fails, we DO NOT send the message.
     * Messages should only be sent with properly resolved template data.
     */
    private void sendEmail(com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String email = caseEntity.getLoan().getPrimaryCustomer().getEmail();
        if (email == null || email.isEmpty()) {
            throw new BusinessException("Email not available for case: " + caseEntity.getId());
        }

        // Template ID is required for Email
        if (action.getTemplateId() == null || action.getTemplateId().isEmpty()) {
            throw new BusinessException("Template ID is required for Email action on case: " + caseEntity.getId());
        }

        // Resolve template - this MUST succeed for message to be sent
        com.finx.strategyengineservice.client.dto.TemplateResolveRequest resolveRequest =
            com.finx.strategyengineservice.client.dto.TemplateResolveRequest.builder()
                .caseId(caseEntity.getId())
                .additionalContext(null)
                .build();

        Long templateId = Long.parseLong(action.getTemplateId());

        com.finx.strategyengineservice.domain.dto.CommonResponse<com.finx.strategyengineservice.client.dto.TemplateResolveResponse> response;
        try {
            response = templateServiceClient.resolveTemplate(templateId, resolveRequest);
        } catch (Exception e) {
            log.error("Failed to resolve template {} for Email case {}: {}", templateId, caseEntity.getId(), e.getMessage());
            throw new BusinessException("Template resolution failed for Email case " + caseEntity.getId() + ": " + e.getMessage());
        }

        if (response == null || response.getPayload() == null) {
            throw new BusinessException("Template resolution returned empty response for Email case: " + caseEntity.getId());
        }

        com.finx.strategyengineservice.client.dto.TemplateResolveResponse resolveResponse = response.getPayload();

        // Validate providerTemplateId - MSG91 template_id
        String msg91TemplateId = resolveResponse.getProviderTemplateId();
        if (msg91TemplateId == null || msg91TemplateId.isEmpty()) {
            throw new BusinessException("MSG91 provider template ID is missing in resolution response for Email case: " + caseEntity.getId() +
                ". Make sure the template was synced with MSG91 and has a valid provider_template_id.");
        }

        // Get resolved variables and convert to Map<String, String>
        Map<String, String> variables = new HashMap<>();
        if (resolveResponse.getResolvedVariables() != null) {
            for (Map.Entry<String, Object> entry : resolveResponse.getResolvedVariables().entrySet()) {
                variables.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }

        if (variables.isEmpty()) {
            log.warn("No resolved variables found for Email template {} case {}", templateId, caseEntity.getId());
        }

        log.info("Resolved {} variables for Email template {} case {}", variables.size(), templateId, caseEntity.getId());

        String customerName = caseEntity.getLoan().getPrimaryCustomer().getFullName();

        com.finx.strategyengineservice.client.dto.EmailRequest request = com.finx.strategyengineservice.client.dto.EmailRequest
                .builder()
                .toEmail(email)
                .toName(customerName)
                .templateId(msg91TemplateId) // MSG91 template_id returned during template creation
                .variables(variables)
                .caseId(caseEntity.getId())
                .build();

        log.info("Sending Email for case: {} to: {} using MSG91 template ID: {}", caseEntity.getId(), email, msg91TemplateId);

        communicationClient.sendEmail(request);
        log.info("Email sent successfully for case: {} using MSG91 template ID: {}", caseEntity.getId(), msg91TemplateId);
    }

    /**
     * Send WhatsApp via communication service
     * Uses template resolution to get:
     * - providerTemplateId (MSG91 template_id returned during template creation)
     * - languageShortCode for the correct language
     * - resolvedVariables for dynamic components
     *
     * IMPORTANT: If template resolution fails, we DO NOT send the message.
     * Messages should only be sent with properly resolved template data.
     */
    private void sendWhatsApp(com.finx.strategyengineservice.domain.entity.Case caseEntity,
            com.finx.strategyengineservice.domain.entity.StrategyAction action) {

        String mobile = caseEntity.getLoan().getPrimaryCustomer().getMobileNumber();
        if (mobile == null || mobile.isEmpty()) {
            throw new BusinessException("Mobile number not available for case: " + caseEntity.getId());
        }

        // Add India country code (91) if not present - MSG91 requires country code
        mobile = formatMobileWithCountryCode(mobile);

        // Template ID is required for WhatsApp
        if (action.getTemplateId() == null || action.getTemplateId().isEmpty()) {
            throw new BusinessException("Template ID is required for WhatsApp action on case: " + caseEntity.getId());
        }

        // Resolve template - this MUST succeed for message to be sent
        com.finx.strategyengineservice.client.dto.TemplateResolveRequest resolveRequest =
            com.finx.strategyengineservice.client.dto.TemplateResolveRequest.builder()
                .caseId(caseEntity.getId())
                .additionalContext(null)
                .build();

        Long templateId = Long.parseLong(action.getTemplateId());

        com.finx.strategyengineservice.domain.dto.CommonResponse<com.finx.strategyengineservice.client.dto.TemplateResolveResponse> response;
        try {
            response = templateServiceClient.resolveTemplate(templateId, resolveRequest);
        } catch (Exception e) {
            log.error("Failed to resolve template {} for case {}: {}", templateId, caseEntity.getId(), e.getMessage());
            throw new BusinessException("Template resolution failed for case " + caseEntity.getId() + ": " + e.getMessage());
        }

        if (response == null || response.getPayload() == null) {
            throw new BusinessException("Template resolution returned empty response for case: " + caseEntity.getId());
        }

        com.finx.strategyengineservice.client.dto.TemplateResolveResponse resolveResponse = response.getPayload();

        // Validate required fields from resolution
        // Use providerTemplateId (MSG91 template_id) for sending messages, NOT templateCode
        String msg91TemplateId = resolveResponse.getProviderTemplateId();
        if (msg91TemplateId == null || msg91TemplateId.isEmpty()) {
            throw new BusinessException("MSG91 provider template ID is missing in resolution response for case: " + caseEntity.getId() +
                ". Make sure the template was synced with MSG91 and has a valid provider_template_id.");
        }

        String languageCode = resolveResponse.getLanguageShortCode();
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = "en_US"; // Default language is acceptable
            log.warn("Language code not found in template resolution, using default: en_US");
        }

        // Build components from resolved variables
        java.util.Map<String, java.util.Map<String, String>> components = new java.util.HashMap<>();
        if (resolveResponse.getResolvedVariables() != null && !resolveResponse.getResolvedVariables().isEmpty()) {
            for (java.util.Map.Entry<String, Object> entry : resolveResponse.getResolvedVariables().entrySet()) {
                java.util.Map<String, String> component = new java.util.HashMap<>();
                component.put("type", "text");
                component.put("value", entry.getValue() != null ? entry.getValue().toString() : "");
                components.put(entry.getKey(), component);
            }
            log.info("Built {} WhatsApp components from template resolution for case: {}", components.size(), caseEntity.getId());
        } else {
            log.warn("No resolved variables found for template {} case {}, sending without components", templateId, caseEntity.getId());
        }

        // Build and send WhatsApp request
        com.finx.strategyengineservice.client.dto.WhatsAppRequest request =
                com.finx.strategyengineservice.client.dto.WhatsAppRequest.builder()
                .templateId(msg91TemplateId) // MSG91 template_id returned during template creation
                .to(java.util.Collections.singletonList(mobile))
                .components(components)
                .language(com.finx.strategyengineservice.client.dto.WhatsAppRequest.WhatsAppLanguage.builder()
                        .code(languageCode)
                        .policy("deterministic")
                        .build())
                .caseId(caseEntity.getId())
                .build();

        log.info("Sending WhatsApp for case: {} using MSG91 template ID: {} with {} components",
                caseEntity.getId(), msg91TemplateId, components.size());

        communicationClient.sendWhatsApp(request);
        log.info("WhatsApp sent successfully for case: {} using MSG91 template ID: {}", caseEntity.getId(), msg91TemplateId);
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
        // Use Pageable.unpaged() to get all results, or use a large page size
        List<StrategyExecution> executions = executionRepository
                .findAllByOrderByStartedAtDesc(org.springframework.data.domain.PageRequest.of(0, 1000))
                .getContent();

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

    /**
     * Format mobile number with India country code (91) for MSG91
     * MSG91 requires phone numbers with country code prefix
     *
     * @param mobile the mobile number (may or may not have country code)
     * @return mobile number with 91 prefix
     */
    private String formatMobileWithCountryCode(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return mobile;
        }

        // Remove any spaces, dashes, or special characters
        String cleanMobile = mobile.replaceAll("[^0-9+]", "");

        // If already has + prefix with country code, just remove the +
        if (cleanMobile.startsWith("+")) {
            return cleanMobile.substring(1);
        }

        // If already starts with 91 and is 12 digits (91 + 10 digit number), return as is
        if (cleanMobile.startsWith("91") && cleanMobile.length() == 12) {
            return cleanMobile;
        }

        // If it's a 10-digit Indian number, add 91 prefix
        if (cleanMobile.length() == 10) {
            return "91" + cleanMobile;
        }

        // Return as is for other cases (might be international number)
        return cleanMobile;
    }
}
