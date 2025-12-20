package com.finx.collectionsservice.domain.dto;

import com.finx.collectionsservice.domain.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryDTO {
    private Long id;
    private Long approvalRequestId;
    private String action;
    private Integer actionLevel;
    private ApprovalStatus fromStatus;
    private ApprovalStatus toStatus;
    private Long actorId;
    private String actorName;
    private String actorRole;
    private String remarks;
    private BigDecimal approvedAmount;
    private Map<String, Object> metadata;
    private LocalDateTime actionTimestamp;
}
