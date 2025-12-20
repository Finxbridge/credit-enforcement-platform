package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Agency Dashboard DTO
 * Matches frontend agency.types.ts AgencyDashboard interface
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyDashboardDTO {

    private Long agencyId;
    private String agencyName;

    @JsonProperty("totalCases")
    private Integer totalCases;

    @JsonProperty("activeCases")
    private Integer activeCases;

    @JsonProperty("resolvedCases")
    private Integer resolvedCases;

    @JsonProperty("totalCollected")
    private BigDecimal totalCollected;

    @JsonProperty("commissionEarned")
    private BigDecimal commissionEarned;

    @JsonProperty("avgResolutionDays")
    private Integer avgResolutionDays;

    @JsonProperty("ptpKeptRate")
    private BigDecimal ptpKeptRate;

    @JsonProperty("ptpBrokenRate")
    private BigDecimal ptpBrokenRate;
}
