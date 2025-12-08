package com.finx.communication.domain.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Mobile number is required")
    private String mobileNumber;

    private String message;

    // Optional - will be fetched from third_party_integration_master if not provided
    private String gatewayName; // FINXBRIDGE, PHONEPE, RAZORPAY

    // For internal tracking
    private Long caseId;
    private String loanAccountNumber;
    private String customerName;
    private String customerEmail;
}
