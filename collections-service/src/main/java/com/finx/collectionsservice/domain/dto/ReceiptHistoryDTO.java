package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptHistoryDTO {

    private Long id;
    private Long receiptId;
    private String receiptNumber;
    private String action;
    private String fromStatus;
    private String toStatus;
    private Long actorId;
    private String actorName;
    private String remarks;
    private String metadata;
    private LocalDateTime actionTimestamp;
}
