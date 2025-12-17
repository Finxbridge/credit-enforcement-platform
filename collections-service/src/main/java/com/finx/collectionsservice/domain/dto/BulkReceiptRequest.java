package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ReceiptFormat;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkReceiptRequest {

    @NotEmpty(message = "At least one repayment ID is required")
    private List<Long> repaymentIds;

    private ReceiptFormat format;

    private Long templateId;

    private Boolean sendEmail;

    private Boolean sendSms;
}
