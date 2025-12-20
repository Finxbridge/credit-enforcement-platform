package com.finx.noticemanagementservice.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoutingRuleRequest {

    @Size(max = 200, message = "Rule name must not exceed 200 characters")
    private String ruleName;

    private String description;

    private Integer rulePriority;

    private Map<String, Object> criteria;

    private Long primaryVendorId;

    private Long secondaryVendorId;

    private Long fallbackVendorId;

    private Integer dispatchSlaHours;

    private Integer deliverySlaDays;

    private BigDecimal maxCostPerDispatch;

    private Boolean isActive;

    private LocalDate validFrom;

    private LocalDate validUntil;
}
