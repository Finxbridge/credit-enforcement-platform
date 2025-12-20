package com.finx.noticemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuleDTO {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String description;
    private Integer rulePriority;
    private Map<String, Object> criteria;
    private Long primaryVendorId;
    private String primaryVendorName;
    private Long secondaryVendorId;
    private String secondaryVendorName;
    private Long fallbackVendorId;
    private String fallbackVendorName;
    private Integer dispatchSlaHours;
    private Integer deliverySlaDays;
    private BigDecimal maxCostPerDispatch;
    private Boolean isActive;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
