package com.finx.configurationsservice.domain.dto;

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
public class CurrencyDTO {
    private Long id;
    private String currencyCode;
    private String currencyName;
    private String currencySymbol;
    private Integer decimalPlaces;
    private BigDecimal exchangeRate;
    private LocalDateTime exchangeRateUpdatedAt;
    private Boolean isBaseCurrency;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
