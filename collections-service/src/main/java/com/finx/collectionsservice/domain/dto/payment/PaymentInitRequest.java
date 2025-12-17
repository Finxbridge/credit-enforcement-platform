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

    // Required for PAYMENT_LINK
    private String mobileNumber;

    // Required for COLLECT_CALL
    private String instrumentType;      // VPA or MOBILE
    private String instrumentReference; // UPI ID or mobile number

    // Optional message for payment link
    private String message;

    // For tracking - caseId is mandatory for linking payment to case
    @NotNull(message = "Case ID is required")
    private Long caseId;

    private String loanAccountNumber;
    private String customerName;
    private String customerEmail;
}
