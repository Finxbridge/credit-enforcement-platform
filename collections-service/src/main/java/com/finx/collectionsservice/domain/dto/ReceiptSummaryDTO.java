package com.finx.collectionsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptSummaryDTO {

    private Long totalReceipts;
    private Long pendingReceipts;
    private Long generatedReceipts;
    private Long sentReceipts;
    private Long downloadedReceipts;
    private Long cancelledReceipts;
    private Long voidReceipts;

    private BigDecimal totalAmount;
    private BigDecimal todayAmount;
    private Long todayCount;

    // By payment mode
    private Long cashReceipts;
    private Long chequeReceipts;
    private Long onlineReceipts;
    private Long upiReceipts;

    private BigDecimal cashAmount;
    private BigDecimal chequeAmount;
    private BigDecimal onlineAmount;
    private BigDecimal upiAmount;
}
