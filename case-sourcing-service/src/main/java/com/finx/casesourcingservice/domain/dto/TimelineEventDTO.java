package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for individual timeline event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventDTO {

    private Long eventId;
    private String eventType; // CALL, PTP, PAYMENT, NOTE, SMS, EMAIL, WHATSAPP, ALLOCATION, STATUS_CHANGE
    private String eventSubType;
    private String eventTitle;
    private String eventDescription;
    private LocalDateTime eventTimestamp;

    // User who performed the action
    private Long userId;
    private String userName;

    // Event-specific data
    private String disposition;
    private String subDisposition;
    private String contactResult;
    private Integer callDurationSeconds;
    private String callRecordingUrl;

    // PTP data
    private BigDecimal ptpAmount;
    private String ptpDate;
    private String ptpStatus;

    // Payment data
    private BigDecimal paymentAmount;
    private String paymentMode;
    private String paymentStatus;
    private String receiptNumber;

    // Communication data
    private String messageChannel;
    private String messageStatus;
    private String messageContent;

    // Additional metadata
    private Map<String, Object> metadata;

    // UI display fields
    private String iconType; // For frontend display
    private String colorCode; // For UI styling
}
