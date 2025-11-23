package com.finx.templatemanagementservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common API response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;

    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> successMessage(String message) {
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> CommonResponse<T> error(String message, String error) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}
