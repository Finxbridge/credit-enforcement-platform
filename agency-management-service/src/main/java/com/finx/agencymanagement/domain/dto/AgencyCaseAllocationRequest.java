package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agency Case Allocation Request DTO
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyCaseAllocationRequest {

    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    @NotEmpty(message = "At least one case ID is required")
    private List<Long> caseIds;

    private Long agencyUserId;

    private String notes;
}
