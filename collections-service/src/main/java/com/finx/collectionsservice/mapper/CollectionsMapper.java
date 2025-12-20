package com.finx.collectionsservice.mapper;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.domain.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CollectionsMapper {

    // PTP Mappings
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "daysSinceCommitment", ignore = true)
    @Mapping(target = "daysUntilDue", ignore = true)
    @Mapping(target = "isOverdue", ignore = true)
    PTPResponse toDto(PTPCommitment ptp);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commitmentDate", ignore = true)
    @Mapping(target = "ptpStatus", ignore = true)
    @Mapping(target = "paymentReceivedAmount", ignore = true)
    @Mapping(target = "paymentReceivedDate", ignore = true)
    @Mapping(target = "brokenReason", ignore = true)
    @Mapping(target = "reminderSent", ignore = true)
    @Mapping(target = "reminderSentAt", ignore = true)
    @Mapping(target = "followUpCompleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    PTPCommitment toEntity(CapturePTPRequest request);

    // Repayment Mappings
    @Mapping(target = "caseNumber", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "approverName", ignore = true)
    @Mapping(target = "collectorName", ignore = true)
    RepaymentDTO toDto(Repayment repayment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "repaymentNumber", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "currentApprovalLevel", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "correctionNotes", ignore = true)
    @Mapping(target = "depositRequiredBy", ignore = true)
    @Mapping(target = "depositedAt", ignore = true)
    @Mapping(target = "depositSlaStatus", ignore = true)
    @Mapping(target = "depositSlaBreachHours", ignore = true)
    @Mapping(target = "isReconciled", ignore = true)
    @Mapping(target = "reconciledAt", ignore = true)
    @Mapping(target = "reconciledBy", ignore = true)
    @Mapping(target = "reconciliationBatchId", ignore = true)
    @Mapping(target = "receiptId", ignore = true)
    @Mapping(target = "isOtsPayment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Repayment toEntity(CreateRepaymentRequest request);

    // OTS Mappings
    @Mapping(target = "caseNumber", ignore = true)
    @Mapping(target = "intentCapturedByName", ignore = true)
    OTSRequestDTO toDto(OTSRequest otsRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "otsNumber", ignore = true)
    @Mapping(target = "loanAccountNumber", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "originalOutstanding", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "waiverBreakdown", ignore = true)
    @Mapping(target = "installmentSchedule", ignore = true)
    @Mapping(target = "intentCapturedAt", ignore = true)
    @Mapping(target = "intentCapturedBy", ignore = true)
    @Mapping(target = "requestRaisedAt", ignore = true)
    @Mapping(target = "requestRaisedBy", ignore = true)
    @Mapping(target = "requestNotes", ignore = true)
    @Mapping(target = "otsStatus", ignore = true)
    @Mapping(target = "currentApprovalLevel", ignore = true)
    @Mapping(target = "maxApprovalLevel", ignore = true)
    @Mapping(target = "letterId", ignore = true)
    @Mapping(target = "letterGeneratedAt", ignore = true)
    @Mapping(target = "letterDownloadedAt", ignore = true)
    @Mapping(target = "letterDownloadedBy", ignore = true)
    @Mapping(target = "settledAt", ignore = true)
    @Mapping(target = "settledAmount", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "expiredAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OTSRequest toEntity(CreateOTSRequest request);

    // Archival Rule Mappings
    ArchivalRuleDTO toArchivalRuleDTO(ArchivalRule rule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "executionCount", ignore = true)
    @Mapping(target = "lastExecutionAt", ignore = true)
    @Mapping(target = "lastExecutionResult", ignore = true)
    @Mapping(target = "lastCasesArchived", ignore = true)
    @Mapping(target = "nextExecutionAt", ignore = true)
    @Mapping(target = "totalCasesArchived", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ArchivalRule toEntity(CreateArchivalRuleRequest request);

    // Settlement Letter Mappings
    SettlementLetterDTO toSettlementLetterDTO(SettlementLetter letter);

    // Receipt Mappings
    ReceiptDTO toReceiptDTO(Receipt receipt);
}
