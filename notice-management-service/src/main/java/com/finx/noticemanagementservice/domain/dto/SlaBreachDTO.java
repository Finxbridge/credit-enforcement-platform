package com.finx.noticemanagementservice.domain.dto;

import com.finx.noticemanagementservice.domain.enums.BreachSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaBreachDTO {
    private Long id;
    private String breachId;
    private String breachType;
    private String entityType;
    private Long entityId;
    private String entityReference;
    private Integer slaHours;
    private LocalDateTime expectedBy;
    private LocalDateTime breachedAt;
    private Integer breachDurationHours;
    private Long vendorId;
    private String vendorName;
    private Long assignedUserId;
    private String assignedUserName;
    private BreachSeverity breachSeverity;
    private Boolean isEscalated;
    private LocalDateTime escalatedAt;
    private Long escalatedTo;
    private Long escalatedBy;
    private Integer escalationLevel;
    private String escalationNotes;
    private Boolean isResolved;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
    private String resolutionNotes;
    private String resolutionAction;
    private BigDecimal penaltyAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
