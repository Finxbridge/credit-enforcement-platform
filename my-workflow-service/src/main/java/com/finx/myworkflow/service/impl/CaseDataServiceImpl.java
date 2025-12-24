package com.finx.myworkflow.service.impl;

import com.finx.myworkflow.domain.dto.AllocationHistoryDTO;
import com.finx.myworkflow.domain.dto.AuditLogDTO;
import com.finx.myworkflow.domain.dto.CaseEventDTO;
import com.finx.myworkflow.domain.dto.CaseSummaryDTO;
import com.finx.myworkflow.domain.dto.CaseTabsDataDTO;
import com.finx.myworkflow.domain.dto.WorkflowCaseListDTO;
import com.finx.myworkflow.domain.entity.*;
import com.finx.myworkflow.domain.enums.AuditAction;
import com.finx.myworkflow.repository.*;
import com.finx.myworkflow.service.AuditLogService;
import com.finx.myworkflow.service.CaseDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CaseDataServiceImpl implements CaseDataService {

    private final WorkflowCaseRepository workflowCaseRepository;
    private final RepaymentRepository repaymentRepository;
    private final PTPRepository ptpRepository;
    private final NoticeRepository noticeRepository;
    private final CaseEventRepository caseEventRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    @Override
    public Map<String, Object> getCasesForWorkflow(Long userId, boolean isAdmin, int page, int size) {
        log.info("Getting cases for workflow - userId: {}, isAdmin: {}, page: {}, size: {}", userId, isAdmin, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Case> casePage;

        // Direct DB query for allocated cases
        // - Admin sees all allocated cases
        // - Collector sees only their allocated cases
        if (isAdmin) {
            casePage = workflowCaseRepository.findAllAllocatedCases(pageable);
        } else {
            casePage = workflowCaseRepository.findAllocatedCasesByUserId(userId, pageable);
        }

        // Map entities to DTOs
        List<WorkflowCaseListDTO> caseList = casePage.getContent().stream()
                .map(this::mapToWorkflowCaseListDTO)
                .collect(Collectors.toList());

        // Build paginated response matching frontend expected format
        Map<String, Object> result = new HashMap<>();
        result.put("content", caseList);
        result.put("totalElements", casePage.getTotalElements());
        result.put("totalPages", casePage.getTotalPages());
        result.put("size", casePage.getSize());
        result.put("number", casePage.getNumber());

        log.info("Found {} allocated cases for workflow", caseList.size());
        return result;
    }

    /**
     * Map Case entity to WorkflowCaseListDTO
     * Matches frontend WorkflowCaseListItem interface
     */
    private WorkflowCaseListDTO mapToWorkflowCaseListDTO(Case caseEntity) {
        return WorkflowCaseListDTO.builder()
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .loanAccountNumber(caseEntity.getLoan() != null ? caseEntity.getLoan().getLoanAccountNumber() : null)
                .customerName(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null
                        ? caseEntity.getLoan().getPrimaryCustomer().getFullName() : null)
                .mobileNumber(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null
                        ? caseEntity.getLoan().getPrimaryCustomer().getMobileNumber() : null)
                .lender(caseEntity.getLoan() != null ? caseEntity.getLoan().getLender() : null)
                .dpd(caseEntity.getLoan() != null ? caseEntity.getLoan().getDpd() : null)
                .bucket(caseEntity.getLoan() != null ? caseEntity.getLoan().getBucket() : null)
                .region(caseEntity.getLocation())
                .city(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null
                        ? caseEntity.getLoan().getPrimaryCustomer().getCity() : caseEntity.getCityCode())
                .state(caseEntity.getLoan() != null && caseEntity.getLoan().getPrimaryCustomer() != null
                        ? caseEntity.getLoan().getPrimaryCustomer().getState() : caseEntity.getStateCode())
                .totalOutstanding(caseEntity.getLoan() != null ? caseEntity.getLoan().getTotalOutstanding() : null)
                .overdueAmount(caseEntity.getLoan() != null ? caseEntity.getLoan().getTotalOutstanding() : null)
                .caseStatus(caseEntity.getCaseStatus())
                .allocatedToUserId(caseEntity.getAllocatedToUserId())
                .allocatedAgent(caseEntity.getPrimaryAgent())
                .lastEventDate(caseEntity.getUpdatedAt())
                .createdAt(caseEntity.getAllocatedAt())
                .build();
    }

    @Override
    public CaseSummaryDTO getCaseSummary(Long caseId, Long userId) {
        log.info("Getting case summary for case: {} by user: {}", caseId, userId);

        Optional<Case> caseOpt = workflowCaseRepository.findByIdWithDetails(caseId);
        if (caseOpt.isEmpty()) {
            log.warn("Case not found: {}", caseId);
            return null;
        }

        Case caseEntity = caseOpt.get();
        LoanDetails loan = caseEntity.getLoan();
        Customer customer = loan != null ? loan.getPrimaryCustomer() : null;

        CaseSummaryDTO summary = CaseSummaryDTO.builder()
                .caseId(caseId)
                .caseNumber(caseEntity.getCaseNumber())
                .status(caseEntity.getCaseStatus())
                .customerName(customer != null ? customer.getFullName() : null)
                .primaryPhone(customer != null ? customer.getMobileNumber() : null)
                .loanAccountNumber(loan != null ? loan.getLoanAccountNumber() : null)
                .totalDue(loan != null ? loan.getTotalOutstanding() : null)
                .dpd(loan != null ? loan.getDpd() : null)
                .bucket(loan != null ? loan.getBucket() : null)
                .assignedAgent(caseEntity.getPrimaryAgent())
                .build();

        // Audit log
        auditLogService.logAction(AuditAction.VIEW, "Case", caseId, caseId, userId,
                "Case summary viewed");

        return summary;
    }

    @Override
    public CaseTabsDataDTO getAllTabsData(Long caseId, Long userId) {
        log.info("Getting all tabs data for case: {} by user: {}", caseId, userId);

        CaseTabsDataDTO.CaseTabsDataDTOBuilder builder = CaseTabsDataDTO.builder().caseId(caseId);

        try {
            builder.loanDetails(getLoanDetails(caseId));
            builder.customerDetails(getCustomerDetails(caseId));
            builder.repayments(getRepaymentHistory(caseId));
            builder.ptps(getPtpHistory(caseId));
            builder.notices(getNotices(caseId));
            builder.callLogs(getCallLogs(caseId));
            builder.smsHistory(getSmsHistory(caseId));
            builder.emailHistory(getEmailHistory(caseId));
            builder.documents(getDocuments(caseId));

            // Get case events
            List<CaseEventDTO> events = getAllCaseEvents(caseId);
            builder.events(events);

            // Get allocation history
            List<AllocationHistoryDTO> allocationHistory = getAllAllocationHistory(caseId);
            builder.allocationHistory(allocationHistory);

            // Get audit trail
            List<AuditLogDTO> auditTrail = getAllAuditLogs(caseId);
            builder.auditTrail(auditTrail);

        } catch (Exception e) {
            log.error("Error fetching tabs data for case {}: {}", caseId, e.getMessage());
        }

        // Audit log
        auditLogService.logAction(AuditAction.VIEW, "CaseTabsData", caseId, caseId, userId,
                "All case tabs data viewed");

        return builder.build();
    }

    @Override
    public Map<String, Object> getLoanDetails(Long caseId) {
        log.debug("Getting loan details for case: {}", caseId);
        Optional<Case> caseOpt = workflowCaseRepository.findByIdWithDetails(caseId);
        if (caseOpt.isEmpty() || caseOpt.get().getLoan() == null) {
            return Collections.emptyMap();
        }

        LoanDetails loan = caseOpt.get().getLoan();
        Map<String, Object> result = new HashMap<>();

        // Account identification
        result.put("loanAccountNumber", loan.getLoanAccountNumber());
        result.put("lender", loan.getLender());
        result.put("coLender", loan.getCoLender());
        result.put("productType", loan.getProductType());
        result.put("schemeCode", loan.getSchemeCode());

        // Amounts
        result.put("loanAmount", loan.getLoanAmount());
        result.put("totalOutstanding", loan.getTotalOutstanding());
        result.put("pos", loan.getPos());
        result.put("tos", loan.getTos());
        result.put("emiAmount", loan.getEmiAmount());
        result.put("penaltyAmount", loan.getPenaltyAmount());
        result.put("charges", loan.getCharges());
        result.put("odInterest", loan.getOdInterest());

        // Overdue breakdown
        result.put("principalOverdue", loan.getPrincipalOverdue());
        result.put("interestOverdue", loan.getInterestOverdue());
        result.put("feesOverdue", loan.getFeesOverdue());
        result.put("penaltyOverdue", loan.getPenaltyOverdue());

        // EMI details
        result.put("emiStartDate", loan.getEmiStartDate());
        result.put("noOfPaidEmi", loan.getNoOfPaidEmi());
        result.put("noOfPendingEmi", loan.getNoOfPendingEmi());
        result.put("emiOverdueFrom", loan.getEmiOverdueFrom());
        result.put("nextEmiDate", loan.getNextEmiDate());

        // DPD & Bucket
        result.put("dpd", loan.getDpd());
        result.put("bucket", loan.getBucket());
        result.put("riskBucket", loan.getRiskBucket());
        result.put("somBucket", loan.getSomBucket());
        result.put("somDpd", loan.getSomDpd());
        result.put("cycleDue", loan.getCycleDue());

        // Rates & Duration
        result.put("roi", loan.getRoi());
        result.put("loanDuration", loan.getLoanDuration());

        // Dates
        result.put("loanDisbursementDate", loan.getLoanDisbursementDate());
        result.put("loanMaturityDate", loan.getLoanMaturityDate());
        result.put("dueDate", loan.getDueDate());

        // Payment info
        result.put("lastPaymentDate", loan.getLastPaymentDate());
        result.put("lastPaymentMode", loan.getLastPaymentMode());
        result.put("lastPaidAmount", loan.getLastPaidAmount());

        // Bank details
        result.put("beneficiaryAccountNumber", loan.getBeneficiaryAccountNumber());
        result.put("beneficiaryAccountName", loan.getBeneficiaryAccountName());
        result.put("repaymentBankName", loan.getRepaymentBankName());
        result.put("repaymentIfscCode", loan.getRepaymentIfscCode());

        return result;
    }

    @Override
    public Map<String, Object> getCustomerDetails(Long caseId) {
        log.debug("Getting customer details for case: {}", caseId);
        Optional<Case> caseOpt = workflowCaseRepository.findByIdWithDetails(caseId);
        if (caseOpt.isEmpty() || caseOpt.get().getLoan() == null || caseOpt.get().getLoan().getPrimaryCustomer() == null) {
            return Collections.emptyMap();
        }

        Customer customer = caseOpt.get().getLoan().getPrimaryCustomer();
        Map<String, Object> result = new HashMap<>();

        // Basic info
        result.put("customerId", customer.getCustomerId());
        result.put("customerCode", customer.getCustomerCode());
        result.put("fullName", customer.getFullName());

        // Contact numbers
        result.put("mobileNumber", customer.getMobileNumber());
        result.put("secondaryMobileNumber", customer.getSecondaryMobileNumber());
        result.put("resiPhone", customer.getResiPhone());
        result.put("additionalPhone2", customer.getAdditionalPhone2());
        result.put("email", customer.getEmail());

        // Address
        result.put("primaryAddress", customer.getPrimaryAddress());
        result.put("secondaryAddress", customer.getSecondaryAddress());
        result.put("city", customer.getCity());
        result.put("state", customer.getState());
        result.put("pincode", customer.getPincode());

        // Family & Employment
        result.put("fatherSpouseName", customer.getFatherSpouseName());
        result.put("employerOrBusinessEntity", customer.getEmployerOrBusinessEntity());

        // References
        result.put("reference1Name", customer.getReference1Name());
        result.put("reference1Number", customer.getReference1Number());
        result.put("reference2Name", customer.getReference2Name());
        result.put("reference2Number", customer.getReference2Number());

        // Preferences
        result.put("languagePreference", customer.getLanguagePreference());

        return result;
    }

    @Override
    public List<Map<String, Object>> getRepaymentHistory(Long caseId) {
        log.debug("Getting repayment history for case: {}", caseId);
        List<Repayment> repayments = repaymentRepository.findByCaseIdOrderByPaymentDateDesc(caseId);

        return repayments.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("repaymentNumber", r.getRepaymentNumber());
            map.put("amount", r.getPaymentAmount());
            map.put("paymentDate", r.getPaymentDate());
            map.put("paymentMode", r.getPaymentMode());
            map.put("status", r.getApprovalStatus());
            map.put("notes", r.getNotes());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPtpHistory(Long caseId) {
        log.debug("Getting PTP history for case: {}", caseId);
        List<PTPCommitment> ptps = ptpRepository.findByCaseIdOrderByPtpDateDesc(caseId);

        return ptps.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("ptpDate", p.getPtpDate());
            map.put("ptpAmount", p.getPtpAmount());
            map.put("commitmentDate", p.getCommitmentDate());
            map.put("ptpStatus", p.getPtpStatus());
            map.put("paymentReceivedAmount", p.getPaymentReceivedAmount());
            map.put("paymentReceivedDate", p.getPaymentReceivedDate());
            map.put("brokenReason", p.getBrokenReason());
            map.put("notes", p.getNotes());
            map.put("createdAt", p.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getNotices(Long caseId) {
        log.debug("Getting notices for case: {}", caseId);
        List<Notice> notices = noticeRepository.findByCaseIdOrderByCreatedAtDesc(caseId);

        return notices.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("noticeNumber", n.getNoticeNumber());
            map.put("noticeType", n.getNoticeType());
            map.put("noticeSubtype", n.getNoticeSubtype());
            map.put("status", n.getStatus());
            map.put("recipientName", n.getRecipientName());
            map.put("recipientAddress", n.getRecipientAddress());
            map.put("pdfUrl", n.getPdfUrl());
            map.put("sentAt", n.getSentAt());
            map.put("deliveredAt", n.getDeliveredAt());
            map.put("createdAt", n.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCallLogs(Long caseId) {
        log.debug("Getting call logs for case: {}", caseId);
        // Call logs from communication service - not in shared DB
        // Return empty for now
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getSmsHistory(Long caseId) {
        log.debug("Getting SMS history for case: {}", caseId);
        // SMS history from communication service - not in shared DB
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getEmailHistory(Long caseId) {
        log.debug("Getting email history for case: {}", caseId);
        // Email history from communication service - not in shared DB
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getDocuments(Long caseId) {
        log.debug("Getting documents for case: {}", caseId);
        // Documents from DMS service - would need DMS tables or keep client call
        // Return empty for now
        return Collections.emptyList();
    }

    @Override
    public Page<CaseEventDTO> getCaseEvents(Long caseId, int page, int size) {
        log.debug("Getting case events for case: {} - page: {}, size: {}", caseId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return caseEventRepository.findByCaseIdOrderByEventTimestampDesc(caseId, pageable)
                .map(this::mapToCaseEventDTO);
    }

    @Override
    public Page<CaseEventDTO> getCaseEventsByCategory(Long caseId, String category, int page, int size) {
        log.debug("Getting case events for case: {} by category: {} - page: {}, size: {}", caseId, category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return caseEventRepository.findByCaseIdAndEventCategoryOrderByEventTimestampDesc(caseId, category, pageable)
                .map(this::mapToCaseEventDTO);
    }

    @Override
    public List<CaseEventDTO> getAllCaseEvents(Long caseId) {
        log.debug("Getting all case events for case: {}", caseId);
        return caseEventRepository.findByCaseIdOrderByEventTimestampDesc(caseId)
                .stream()
                .map(this::mapToCaseEventDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AllocationHistoryDTO> getAllocationHistory(Long caseId, int page, int size) {
        log.debug("Getting allocation history for case: {} - page: {}, size: {}", caseId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return allocationHistoryRepository.findByCaseIdOrderByAllocatedAtDesc(caseId, pageable)
                .map(this::mapToAllocationHistoryDTO);
    }

    @Override
    public List<AllocationHistoryDTO> getAllAllocationHistory(Long caseId) {
        log.debug("Getting all allocation history for case: {}", caseId);
        return allocationHistoryRepository.findByCaseIdOrderByAllocatedAtDesc(caseId)
                .stream()
                .map(this::mapToAllocationHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditLogDTO> getAuditLogs(Long caseId, int page, int size) {
        log.debug("Getting audit logs for case: {} - page: {}, size: {}", caseId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByCaseIdOrderByCreatedAtDesc(caseId, pageable)
                .map(this::mapToAuditLogDTO);
    }

    @Override
    public List<AuditLogDTO> getAllAuditLogs(Long caseId) {
        log.debug("Getting all audit logs for case: {}", caseId);
        Pageable pageable = PageRequest.of(0, 100);
        return auditLogRepository.findByCaseIdOrderByCreatedAtDesc(caseId, pageable)
                .getContent()
                .stream()
                .map(this::mapToAuditLogDTO)
                .collect(Collectors.toList());
    }

    // ========== Mapper Methods ==========

    private CaseEventDTO mapToCaseEventDTO(CaseEvent event) {
        return CaseEventDTO.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .caseId(event.getCaseId())
                .loanAccountNumber(event.getLoanAccountNumber())
                .eventType(event.getEventType())
                .eventSubtype(event.getEventSubtype())
                .eventCategory(event.getEventCategory())
                .eventTitle(event.getEventTitle())
                .eventDescription(event.getEventDescription())
                .eventData(event.getEventData())
                .actorId(event.getActorId())
                .actorName(event.getActorName())
                .actorType(event.getActorType())
                .sourceService(event.getSourceService())
                .relatedEntityType(event.getRelatedEntityType())
                .relatedEntityId(event.getRelatedEntityId())
                .communicationChannel(event.getCommunicationChannel())
                .communicationStatus(event.getCommunicationStatus())
                .communicationId(event.getCommunicationId())
                .fromAgentId(event.getFromAgentId())
                .toAgentId(event.getToAgentId())
                .ptpAmount(event.getPtpAmount())
                .ptpDate(event.getPtpDate())
                .ptpStatus(event.getPtpStatus())
                .paymentAmount(event.getPaymentAmount())
                .paymentMode(event.getPaymentMode())
                .receiptNumber(event.getReceiptNumber())
                .oldStatus(event.getOldStatus())
                .newStatus(event.getNewStatus())
                .eventTimestamp(event.getEventTimestamp())
                .createdAt(event.getCreatedAt())
                .metadata(event.getMetadata())
                .build();
    }

    private AllocationHistoryDTO mapToAllocationHistoryDTO(AllocationHistory history) {
        return AllocationHistoryDTO.builder()
                .id(history.getId())
                .caseId(history.getCaseId())
                .externalCaseId(history.getExternalCaseId())
                .allocatedToUserId(history.getAllocatedToUserId())
                .allocatedToUsername(history.getAllocatedToUsername())
                .newOwnerType(history.getNewOwnerType())
                .allocatedFromUserId(history.getAllocatedFromUserId())
                .previousOwnerType(history.getPreviousOwnerType())
                .action(history.getAction())
                .actionDisplayName(getActionDisplayName(history.getAction()))
                .reason(history.getReason())
                .allocatedBy(history.getAllocatedBy())
                .allocatedAt(history.getAllocatedAt())
                .createdAt(history.getCreatedAt())
                .batchId(history.getBatchId())
                .agencyId(history.getAgencyId())
                .agencyCode(history.getAgencyCode())
                .agencyName(history.getAgencyName())
                .build();
    }

    private AuditLogDTO mapToAuditLogDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .auditId(auditLog.getAuditId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .caseId(auditLog.getCaseId())
                .userId(auditLog.getUserId())
                .userName(auditLog.getUserName())
                .oldValues(auditLog.getOldValues())
                .newValues(auditLog.getNewValues())
                .changes(auditLog.getChanges())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private String getActionDisplayName(String action) {
        if (action == null) return null;
        return switch (action) {
            case "ALLOCATED" -> "Case Allocated";
            case "REALLOCATED" -> "Case Reallocated";
            case "DEALLOCATED" -> "Case Deallocated";
            case "BULK_REALLOCATION" -> "Bulk Reallocation";
            case "AGENT_TRANSFER" -> "Agent Transfer";
            case "RULE_BASED_ALLOCATION" -> "Rule-Based Allocation";
            case "AGENCY_ALLOCATED" -> "Allocated to Agency";
            case "AGENCY_DEALLOCATED" -> "Deallocated from Agency";
            case "AGENT_ASSIGNED" -> "Assigned to Agent";
            case "AGENT_REASSIGNED" -> "Reassigned to Agent";
            case "AGENT_UNASSIGNED" -> "Unassigned from Agent";
            default -> action.replace("_", " ");
        };
    }
}
