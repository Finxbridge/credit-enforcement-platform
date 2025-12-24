package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for capturing PTP commitment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapturePTPRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "PTP date is required")
    @Future(message = "PTP date must be in the future")
    private LocalDate ptpDate;

    @NotNull(message = "PTP amount is required")
    @DecimalMin(value = "1.0", message = "PTP amount must be at least 1")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal ptpAmount;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 50, message = "Call disposition cannot exceed 50 characters")
    private String callDisposition;

    private LocalDate followUpDate;

    /**
     * Template ID from Template Management Service for sending reminders
     * Get from dropdown: GET /templates/dropdown/WHATSAPP
     */
    private Long reminderTemplateId;

    /**
     * Template code for reference (optional, auto-populated from template service)
     */
    @Size(max = 100, message = "Template code cannot exceed 100 characters")
    private String reminderTemplateCode;

    /**
     * Communication channel for reminder
     * Options: WHATSAPP, SMS, EMAIL
     */
    @Size(max = 20, message = "Reminder channel cannot exceed 20 characters")
    private String reminderChannel;
}
