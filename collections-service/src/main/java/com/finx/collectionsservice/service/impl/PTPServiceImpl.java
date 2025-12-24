package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.client.CommunicationServiceClient;
import com.finx.collectionsservice.client.TemplateServiceClient;
import com.finx.collectionsservice.client.dto.CommonResponse;
import com.finx.collectionsservice.client.dto.TemplateResolveRequest;
import com.finx.collectionsservice.client.dto.TemplateResolveResponse;
import com.finx.collectionsservice.client.dto.WhatsAppSendRequest;
import com.finx.collectionsservice.client.dto.WhatsAppSendResponse;
import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.entity.PTPCommitment;
import com.finx.collectionsservice.domain.enums.PTPStatus;
import com.finx.collectionsservice.exception.BusinessException;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.mapper.CollectionsMapper;
import com.finx.collectionsservice.repository.PTPCommitmentRepository;
import com.finx.collectionsservice.service.CaseEventService;
import com.finx.collectionsservice.service.PTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PTPServiceImpl implements PTPService {

    private final PTPCommitmentRepository ptpRepository;
    private final CollectionsMapper mapper;
    private final TemplateServiceClient templateServiceClient;
    private final CommunicationServiceClient communicationServiceClient;
    private final CaseEventService caseEventService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Override
    @Transactional
    public PTPResponse capturePTP(CapturePTPRequest request) {
        log.info("Capturing PTP for case: {}, amount: {}, date: {}, templateId: {}",
                request.getCaseId(), request.getPtpAmount(), request.getPtpDate(), request.getReminderTemplateId());

        PTPCommitment ptp = PTPCommitment.builder()
                .caseId(request.getCaseId())
                .userId(request.getUserId())
                .ptpDate(request.getPtpDate())
                .ptpAmount(request.getPtpAmount())
                .notes(request.getNotes())
                .callDisposition(request.getCallDisposition())
                .followUpDate(request.getFollowUpDate())
                .ptpStatus(PTPStatus.PENDING)
                .createdBy(request.getUserId())
                // Template info for reminders
                .reminderTemplateId(request.getReminderTemplateId())
                .reminderTemplateCode(request.getReminderTemplateCode())
                .reminderChannel(request.getReminderChannel() != null ? request.getReminderChannel() : "WHATSAPP")
                .build();

        PTPCommitment saved = ptpRepository.save(ptp);
        log.info("PTP captured successfully with ID: {}, reminder template: {}",
                saved.getId(), saved.getReminderTemplateId());

        // Log case event for PTP capture
        caseEventService.logPtpCaptured(
                saved.getCaseId(),
                null, // loanAccountNumber not available in PTP
                saved.getId(),
                saved.getPtpAmount(),
                saved.getPtpDate(),
                saved.getUserId(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PTPCaseDTO> getPTPsDue(LocalDate dueDate, Long userId) {
        log.info("Fetching PTPs due on: {} for user: {}", dueDate, userId);

        List<PTPCommitment> ptps;
        if (userId != null) {
            ptps = ptpRepository.findPTPsDueForUser(dueDate, userId);
        } else {
            ptps = ptpRepository.findPTPsDueOnDate(dueDate);
        }

        return ptps.stream()
                .map(this::mapToPTPCaseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PTPCaseDTO> getBrokenPTPs(Long userId) {
        log.info("Fetching broken PTPs for user: {}", userId);

        LocalDate today = LocalDate.now();
        List<PTPCommitment> ptps;

        if (userId != null) {
            ptps = ptpRepository.findBrokenPTPsForUser(today, userId);
        } else {
            ptps = ptpRepository.findBrokenPTPs(today);
        }

        return ptps.stream()
                .map(this::mapToPTPCaseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PTPResponse getPTPById(Long ptpId) {
        log.info("Fetching PTP by ID: {}", ptpId);

        PTPCommitment ptp = ptpRepository.findById(ptpId)
                .orElseThrow(() -> new ResourceNotFoundException("PTP", ptpId));

        return mapToResponse(ptp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PTPResponse> getPTPsByCase(Long caseId) {
        log.info("Fetching all PTPs for case: {}", caseId);

        List<PTPCommitment> ptps = ptpRepository.findByCaseIdOrderByCreatedAtDesc(caseId);

        return ptps.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PTPResponse updatePTPStatus(Long ptpId, UpdatePTPRequest request) {
        log.info("Updating PTP status: {} to {}", ptpId, request.getPtpStatus());

        PTPCommitment ptp = ptpRepository.findById(ptpId)
                .orElseThrow(() -> new ResourceNotFoundException("PTP", ptpId));

        ptp.setPtpStatus(request.getPtpStatus());
        ptp.setPaymentReceivedAmount(request.getPaymentReceivedAmount());
        ptp.setPaymentReceivedDate(request.getPaymentReceivedDate());
        ptp.setBrokenReason(request.getBrokenReason());
        ptp.setFollowUpDate(request.getFollowUpDate());

        if (request.getNotes() != null) {
            ptp.setNotes(ptp.getNotes() != null ?
                    ptp.getNotes() + "\n" + request.getNotes() : request.getNotes());
        }

        String oldStatus = ptp.getPtpStatus() != null ? ptp.getPtpStatus().name() : null;
        PTPCommitment updated = ptpRepository.save(ptp);
        log.info("PTP status updated successfully");

        // Log case event for PTP status change
        caseEventService.logPtpStatusChange(
                updated.getCaseId(),
                null,
                updated.getId(),
                updated.getPtpAmount(),
                updated.getPtpDate(),
                oldStatus,
                request.getPtpStatus().name(),
                null,
                null,
                request.getBrokenReason()
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PTPStatsDTO getPTPStats(Long userId) {
        log.info("Fetching PTP statistics for user: {}", userId);

        List<PTPCommitment> allPTPs;
        if (userId != null) {
            allPTPs = ptpRepository.findByUserId(userId);
        } else {
            allPTPs = ptpRepository.findAll();
        }

        Long pending = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.PENDING).count();
        Long kept = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.KEPT).count();
        Long broken = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.BROKEN).count();
        Long renewed = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.RENEWED).count();
        Long partial = allPTPs.stream().filter(p -> p.getPtpStatus() == PTPStatus.PARTIAL).count();

        BigDecimal totalAmount = allPTPs.stream()
                .map(PTPCommitment::getPtpAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal collectedAmount = allPTPs.stream()
                .filter(p -> p.getPaymentReceivedAmount() != null)
                .map(PTPCommitment::getPaymentReceivedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingAmount = allPTPs.stream()
                .filter(p -> p.getPtpStatus() == PTPStatus.PENDING)
                .map(PTPCommitment::getPtpAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double keepRate = 0.0;
        Double brokenRate = 0.0;
        long totalResolved = kept + broken + partial;

        if (totalResolved > 0) {
            keepRate = ((kept + partial) * 100.0) / totalResolved;
            brokenRate = (broken * 100.0) / totalResolved;
        }

        LocalDate today = LocalDate.now();
        Long ptpsDueToday = allPTPs.stream()
                .filter(p -> p.getPtpDate().equals(today) && p.getPtpStatus() == PTPStatus.PENDING)
                .count();

        Long ptpsOverdueToday = allPTPs.stream()
                .filter(p -> p.getPtpDate().isBefore(today) && p.getPtpStatus() == PTPStatus.PENDING)
                .count();

        BigDecimal amountDueToday = allPTPs.stream()
                .filter(p -> p.getPtpDate().equals(today) && p.getPtpStatus() == PTPStatus.PENDING)
                .map(PTPCommitment::getPtpAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PTPStatsDTO.builder()
                .userId(userId)
                .totalPTPs((long) allPTPs.size())
                .pendingPTPs(pending)
                .keptPTPs(kept)
                .brokenPTPs(broken)
                .renewedPTPs(renewed)
                .partialPTPs(partial)
                .totalPTPAmount(totalAmount)
                .collectedAmount(collectedAmount)
                .pendingAmount(pendingAmount)
                .keepRate(Math.round(keepRate * 100.0) / 100.0)
                .brokenRate(Math.round(brokenRate * 100.0) / 100.0)
                .ptpsDueToday(ptpsDueToday)
                .ptpsOverdueToday(ptpsOverdueToday)
                .amountDueToday(amountDueToday)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PTPResponse> getPTPsByStatus(String status, Pageable pageable) {
        log.info("Fetching PTPs by status: {}", status);

        PTPStatus ptpStatus = PTPStatus.valueOf(status.toUpperCase());
        Page<PTPCommitment> ptps = ptpRepository.findByPtpStatus(ptpStatus, pageable);

        return ptps.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Integer processOverduePTPs() {
        log.info("Processing overdue PTPs");

        LocalDate today = LocalDate.now();
        List<PTPCommitment> brokenPTPs = ptpRepository.findBrokenPTPs(today);

        for (PTPCommitment ptp : brokenPTPs) {
            ptp.setPtpStatus(PTPStatus.BROKEN);
            ptp.setBrokenReason("Auto-marked as broken: Payment not received by due date");
            ptpRepository.save(ptp);
        }

        log.info("Processed {} overdue PTPs", brokenPTPs.size());
        return brokenPTPs.size();
    }

    @Override
    @Transactional
    public Integer sendPTPReminders() {
        log.info("Sending PTP reminders");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<PTPCommitment> ptpsNeedingReminder = ptpRepository.findPTPsRequiringReminder(tomorrow);

        int remindersSent = 0;
        int remindersFailed = 0;

        for (PTPCommitment ptp : ptpsNeedingReminder) {
            try {
                // Only send if template is configured
                if (ptp.getReminderTemplateId() != null) {
                    sendReminderForPTP(ptp);
                    remindersSent++;
                } else {
                    log.warn("PTP {} has no reminder template configured, skipping", ptp.getId());
                }

                // Mark reminder as sent regardless of template
                ptp.setReminderSent(true);
                ptp.setReminderSentAt(LocalDateTime.now());
                ptpRepository.save(ptp);

            } catch (Exception e) {
                log.error("Failed to send reminder for PTP {}: {}", ptp.getId(), e.getMessage());
                remindersFailed++;
                // Still mark as attempted to prevent retry loops
                ptp.setReminderSent(true);
                ptp.setReminderSentAt(LocalDateTime.now());
                ptpRepository.save(ptp);
            }
        }

        log.info("PTP Reminders: Sent={}, Failed={}, Total={}", remindersSent, remindersFailed, ptpsNeedingReminder.size());
        return remindersSent;
    }

    /**
     * Send reminder for a specific PTP using the configured template
     * Resolves template from Template Management Service and sends via Communication Service
     */
    private void sendReminderForPTP(PTPCommitment ptp) {
        log.info("Sending {} reminder for PTP {} (case: {}, template: {})",
                ptp.getReminderChannel(), ptp.getId(), ptp.getCaseId(), ptp.getReminderTemplateId());

        // Build additional context with PTP-specific data
        Map<String, Object> additionalContext = new HashMap<>();
        additionalContext.put("ptp_amount", ptp.getPtpAmount().toString());
        additionalContext.put("ptp_date", ptp.getPtpDate().format(DATE_FORMATTER));
        additionalContext.put("ptp_id", ptp.getId().toString());
        additionalContext.put("days_until_due", ChronoUnit.DAYS.between(LocalDate.now(), ptp.getPtpDate()));

        // Resolve template
        TemplateResolveRequest resolveRequest = TemplateResolveRequest.builder()
                .caseId(ptp.getCaseId())
                .additionalContext(additionalContext)
                .build();

        CommonResponse<TemplateResolveResponse> response;
        try {
            response = templateServiceClient.resolveTemplate(ptp.getReminderTemplateId(), resolveRequest);
        } catch (Exception e) {
            log.error("Failed to resolve template {} for PTP {}: {}",
                    ptp.getReminderTemplateId(), ptp.getId(), e.getMessage());
            throw new BusinessException("Template resolution failed: " + e.getMessage());
        }

        if (response == null || response.getPayload() == null) {
            throw new BusinessException("Template resolution returned empty response for PTP: " + ptp.getId());
        }

        TemplateResolveResponse resolveResponse = response.getPayload();

        // Validate MSG91 template ID
        String msg91TemplateId = resolveResponse.getProviderTemplateId();
        if (msg91TemplateId == null || msg91TemplateId.isEmpty()) {
            throw new BusinessException("MSG91 provider template ID missing for PTP: " + ptp.getId());
        }

        // Send based on channel
        String channel = ptp.getReminderChannel() != null ? ptp.getReminderChannel().toUpperCase() : "WHATSAPP";
        switch (channel) {
            case "WHATSAPP":
                sendWhatsAppReminder(ptp, resolveResponse, msg91TemplateId);
                break;
            case "SMS":
                sendSMSReminder(ptp, resolveResponse, msg91TemplateId);
                break;
            default:
                log.warn("Unsupported reminder channel {} for PTP {}, defaulting to WhatsApp", channel, ptp.getId());
                sendWhatsAppReminder(ptp, resolveResponse, msg91TemplateId);
        }
    }

    /**
     * Send WhatsApp reminder via Communication Service
     * Matches strategy-engine's sendWhatsApp() flow exactly
     */
    private void sendWhatsAppReminder(PTPCommitment ptp, TemplateResolveResponse resolveResponse, String msg91TemplateId) {
        // Get customer mobile from database
        String customerMobile = ptpRepository.findCustomerMobileByCaseId(ptp.getCaseId());
        if (customerMobile == null || customerMobile.isEmpty()) {
            throw new BusinessException("Customer mobile not found for case: " + ptp.getCaseId());
        }

        // Format mobile with country code (91 for India)
        String formattedMobile = formatMobileWithCountryCode(customerMobile);

        String languageCode = resolveResponse.getLanguageShortCode();
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = "en_US"; // Default language is acceptable
            log.warn("Language code not found in template resolution, using default: en_US");
        }

        // Build components from resolved variables using body_1, body_2, body_3 format for MSG91
        // MSG91 expects components keyed as body_1, body_2, etc. based on the order of variables in template
        Map<String, Map<String, String>> components = new LinkedHashMap<>();

        // Add header_1 component ONLY if template was created with a document header
        // Check both hasDocument flag AND headerType to ensure this template has a document header
        Boolean hasDocument = resolveResponse.getHasDocument();
        String headerType = resolveResponse.getHeaderType();
        if (Boolean.TRUE.equals(hasDocument) && headerType != null && !headerType.isEmpty()) {
            // Template was created with a document header - include header_1 component
            String documentUrl = resolveResponse.getProcessedDocumentUrl();
            if (documentUrl == null || documentUrl.isEmpty()) {
                documentUrl = resolveResponse.getOriginalDocumentUrl();
            }

            if (documentUrl != null && !documentUrl.isEmpty()) {
                Map<String, String> headerComponent = new LinkedHashMap<>();
                headerComponent.put("type", headerType.toLowerCase()); // document, image, or video
                headerComponent.put("value", documentUrl);
                components.put("header_1", headerComponent);

                log.info("Added header_1 component for PTP {} (case {}): type={}, url={}",
                        ptp.getId(), ptp.getCaseId(), headerType.toLowerCase(), documentUrl);
            } else {
                log.warn("Template has document header (headerType={}) but no document URL available for PTP {} (case {})",
                        headerType, ptp.getId(), ptp.getCaseId());
            }
        }

        if (resolveResponse.getResolvedVariables() != null && !resolveResponse.getResolvedVariables().isEmpty()) {
            List<String> variableOrder = resolveResponse.getVariableOrder();

            if (variableOrder != null && !variableOrder.isEmpty()) {
                // Use variable order to map to body_1, body_2, etc.
                log.info("Variable order from template: {}", variableOrder);
                log.info("Resolved variables: {}", resolveResponse.getResolvedVariables());

                for (int i = 0; i < variableOrder.size(); i++) {
                    String varName = variableOrder.get(i);
                    Object value = resolveResponse.getResolvedVariables().get(varName);

                    // Also try with {{varName}} format in case keys are stored that way
                    if (value == null) {
                        value = resolveResponse.getResolvedVariables().get("{{" + varName + "}}");
                    }

                    Map<String, String> component = new LinkedHashMap<>();
                    component.put("type", "text");
                    String valueStr = (value != null && !value.toString().isEmpty()) ? value.toString() : "";
                    component.put("value", valueStr);

                    // MSG91 expects body_1, body_2, body_3, etc.
                    String componentKey = "body_" + (i + 1);
                    components.put(componentKey, component);

                    log.info("Mapped variable '{}' to '{}' with value: '{}'", varName, componentKey, valueStr);
                }
                log.info("Built {} WhatsApp components for PTP {} (case {}): hasDocument={}, headerType={}",
                        components.size(), ptp.getId(), ptp.getCaseId(), hasDocument, headerType);
            } else {
                // Fallback: if no variable order, use resolved variables directly (may not work with MSG91)
                log.warn("No variable order found, falling back to direct variable mapping (may not work with MSG91)");
                int index = 1;
                for (Map.Entry<String, Object> entry : resolveResponse.getResolvedVariables().entrySet()) {
                    Map<String, String> component = new LinkedHashMap<>();
                    component.put("type", "text");
                    component.put("value", entry.getValue() != null ? entry.getValue().toString() : "");
                    components.put("body_" + index, component);
                    index++;
                }
            }
        } else {
            log.warn("No resolved variables found for template {} PTP {}, sending without body components",
                    ptp.getReminderTemplateId(), ptp.getId());
        }

        // Build and send WhatsApp request
        WhatsAppSendRequest request = WhatsAppSendRequest.builder()
                .templateId(msg91TemplateId) // MSG91 template_id returned during template creation
                .to(Collections.singletonList(formattedMobile))
                .components(components)
                .language(WhatsAppSendRequest.WhatsAppLanguage.builder()
                        .code(languageCode)
                        .policy("deterministic")
                        .build())
                .caseId(ptp.getCaseId())
                .build();

        log.info("Sending WhatsApp for PTP {} (case {}) using MSG91 template ID: {} with {} components",
                ptp.getId(), ptp.getCaseId(), msg91TemplateId, components.size());

        WhatsAppSendResponse sendResponse = communicationServiceClient.sendWhatsApp(request);
        log.info("WhatsApp sent successfully for PTP {} (case {}) using MSG91 template ID: {}. Status: {}",
                ptp.getId(), ptp.getCaseId(), msg91TemplateId,
                sendResponse != null ? sendResponse.getStatus() : "unknown");
    }

    /**
     * Format mobile number with India country code (91) for MSG91
     */
    private String formatMobileWithCountryCode(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return mobile;
        }

        // Remove spaces, dashes, special characters
        String cleanMobile = mobile.replaceAll("[^0-9+]", "");

        // If already has + prefix, remove it
        if (cleanMobile.startsWith("+")) {
            return cleanMobile.substring(1);
        }

        // If already starts with 91 and is 12 digits, return as is
        if (cleanMobile.startsWith("91") && cleanMobile.length() == 12) {
            return cleanMobile;
        }

        // If 10-digit number, add 91 prefix
        if (cleanMobile.length() == 10) {
            return "91" + cleanMobile;
        }

        return cleanMobile;
    }

    /**
     * Send SMS reminder via Communication Service
     */
    private void sendSMSReminder(PTPCommitment ptp, TemplateResolveResponse resolveResponse, String msg91TemplateId) {
        // For SMS, we'd use a similar pattern but with SMSRequest
        // This is a placeholder - implement based on your SMS DTO structure
        log.info("SMS reminder for PTP {} would be sent using template {}", ptp.getId(), msg91TemplateId);
        // TODO: Implement SMS sending similar to WhatsApp
    }

    private PTPResponse mapToResponse(PTPCommitment ptp) {
        LocalDate today = LocalDate.now();
        long daysSince = ChronoUnit.DAYS.between(ptp.getCommitmentDate().toLocalDate(), today);
        long daysUntil = ChronoUnit.DAYS.between(today, ptp.getPtpDate());
        boolean isOverdue = ptp.getPtpDate().isBefore(today) && ptp.getPtpStatus() == PTPStatus.PENDING;

        PTPResponse response = mapper.toDto(ptp);
        response.setDaysSinceCommitment(daysSince);
        response.setDaysUntilDue(daysUntil);
        response.setIsOverdue(isOverdue);

        return response;
    }

    private PTPCaseDTO mapToPTPCaseDTO(PTPCommitment ptp) {
        LocalDate today = LocalDate.now();
        long daysOverdue = ptp.getPtpDate().isBefore(today) ?
                ChronoUnit.DAYS.between(ptp.getPtpDate(), today) : 0;

        return PTPCaseDTO.builder()
                .caseId(ptp.getCaseId())
                .ptpId(ptp.getId())
                .ptpDate(ptp.getPtpDate())
                .ptpAmount(ptp.getPtpAmount())
                .ptpStatus(ptp.getPtpStatus().name())
                .commitmentDate(ptp.getCommitmentDate().toLocalDate())
                .ptpNotes(ptp.getNotes())
                .daysOverdue(daysOverdue)
                .reminderSent(ptp.getReminderSent())
                .userId(ptp.getUserId())
                .build();
    }
}
