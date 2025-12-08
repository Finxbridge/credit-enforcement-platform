package com.finx.strategyengineservice.domain.enums;

/**
 * Operators for numeric filters
 */
public enum NumericOperator {
    GREATER_THAN_EQUAL(">="),      // Minimum Value
    LESS_THAN_EQUAL("<="),         // Maximum Value
    EQUAL("="),                     // Exact Value
    RANGE("RANGE");                 // Between Min and Max

    private final String symbol;

    NumericOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
