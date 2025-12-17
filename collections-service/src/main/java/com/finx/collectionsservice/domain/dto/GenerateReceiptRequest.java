package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ReceiptFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReceiptRequest {

    @NotNull(message = "Repayment ID is required")
    private Long repaymentId;

    private ReceiptFormat format;

    private Long templateId;

    private String remarks;

    // Additional customer info for receipt
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    // Auto-send options
    private Boolean sendEmail;
    private Boolean sendSms;
}
