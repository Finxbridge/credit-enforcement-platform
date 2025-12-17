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
public class CancelReceiptRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;

    private String remarks;
}
