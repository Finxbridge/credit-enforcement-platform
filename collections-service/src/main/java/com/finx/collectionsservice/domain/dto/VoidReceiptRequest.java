package com.finx.collectionsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoidReceiptRequest {

    @NotBlank(message = "Void reason is required")
    private String reason;

    private String remarks;

    // For approval workflow
    private Long approvalRequestId;
}
