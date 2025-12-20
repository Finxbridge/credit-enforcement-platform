package com.finx.communication.domain.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Unified payment request for all service types
 * Fields are used based on serviceType:
 *
 * DYNAMIC_QR: amount (required)
 * PAYMENT_LINK: amount, mobileNumber (required), message (required - uses default_message from config if not provided)
 * COLLECT_CALL: amount, instrumentType, instrumentReference (required)
 *
 * Note: All API endpoints are read from database configuration (init_endpoint, status_endpoint, etc.)
 * If any required endpoint is not configured, a ConfigurationNotFoundException will be thrown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedPaymentRequest {

    @NotBlank(message = "Service type is required (DYNAMIC_QR, PAYMENT_LINK, COLLECT_CALL)")
    private String serviceType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    // For PAYMENT_LINK
    private String mobileNumber;
    private String message;

    // For COLLECT_CALL
    private String instrumentType;      // VPA or MOBILE
    private String instrumentReference; // UPI ID or mobile number

    // For tracking (optional)
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private String customerEmail;
}
