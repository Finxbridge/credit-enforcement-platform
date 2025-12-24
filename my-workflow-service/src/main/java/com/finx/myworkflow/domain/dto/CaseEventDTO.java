package com.finx.myworkflow.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseEventDTO {

    private Long id;
    private String eventId;
    private Long caseId;
    private String loanAccountNumber;

    // Event Classification
    private String eventType;
    private String eventSubtype;
    private String eventCategory;

    // Event Details
    private String eventTitle;
    private String eventDescription;
    private Map<String, Object> eventData;

    // Actor Information
    private Long actorId;
    private String actorName;
    private String actorType;

    // Source Service
    private String sourceService;

    // Related Entities
    private String relatedEntityType;
    private Long relatedEntityId;

    // Communication specific
    private String communicationChannel;
    private String communicationStatus;
    private Long communicationId;

    // Allocation specific
    private Long fromAgentId;
    private Long toAgentId;

    // PTP specific
    private BigDecimal ptpAmount;
    private LocalDate ptpDate;
    private String ptpStatus;

    // Payment specific
    private BigDecimal paymentAmount;
    private String paymentMode;
    private String receiptNumber;

    // Status Change
    private String oldStatus;
    private String newStatus;

    // Timestamps
    private LocalDateTime eventTimestamp;
    private LocalDateTime createdAt;

    // Metadata
    private Map<String, Object> metadata;
}
