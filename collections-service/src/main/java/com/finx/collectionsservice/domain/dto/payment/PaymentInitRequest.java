package com.finx.collectionsservice.domain.dto.payment;

import com.finx.collectionsservice.domain.enums.PaymentServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Unified payment initiation request
 * Routes to appropriate service based on serviceType
 *
 * Service Type Requirements:
 * - DYNAMIC_QR: amount, caseId (required)
 * - PAYMENT_LINK: amount, caseId, mobileNumber (required), message (optional - uses default from config)
 * - COLLECT_CALL: amount, caseId, mobileNumber (required) - instrumentType is always MOBILE from config
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitRequest {

    @NotNull(message = "Service type is required")
    private PaymentServiceType serviceType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    // Required for PAYMENT_LINK and COLLECT_CALL
    private String mobileNumber;

    // Legacy fields for COLLECT_CALL (instrumentType is now MOBILE from config)
    // Can use mobileNumber instead of instrumentReference
    private String instrumentType;      // Deprecated: Always MOBILE from database config
    private String instrumentReference; // Mobile number (can use mobileNumber field instead)

    // Optional message for payment link (uses default_message from config if not provided)
    private String message;

    // For tracking - caseId is mandatory for linking payment to case
    @NotNull(message = "Case ID is required")
    private Long caseId;

    private String loanAccountNumber;
    private String customerName;
    private String customerEmail;
}
