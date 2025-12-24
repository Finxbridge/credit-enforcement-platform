package com.finx.collectionsservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common API response wrapper matching Template Management Service response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private boolean success;
    private String message;
    private T payload;
    private Object errors;
}
