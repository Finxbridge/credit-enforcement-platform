package com.finx.configurationsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCurrencyRequest {

    @NotBlank(message = "Currency code is required")
    @Size(max = 10, message = "Currency code must be at most 10 characters")
    private String currencyCode;

    @NotBlank(message = "Currency name is required")
    @Size(max = 100, message = "Currency name must be at most 100 characters")
    private String currencyName;

    @NotBlank(message = "Currency symbol is required")
    @Size(max = 10, message = "Currency symbol must be at most 10 characters")
    private String currencySymbol;

    private Integer decimalPlaces;

    private BigDecimal exchangeRate;

    private Boolean isBaseCurrency;
}
