package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agency Approval Request DTO
 * Matches frontend agency.types.ts AgencyApprovalRequest interface
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyApprovalRequest {

    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    @NotNull(message = "Approved flag is required")
    private Boolean approved;

    private String notes;

    private String reason;
}
