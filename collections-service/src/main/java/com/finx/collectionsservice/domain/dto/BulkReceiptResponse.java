package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkReceiptResponse {

    private Integer totalRequested;
    private Integer successCount;
    private Integer failedCount;
    private List<ReceiptDTO> generatedReceipts;
    private List<ReceiptGenerationError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptGenerationError {
        private Long repaymentId;
        private String errorMessage;
    }
}
