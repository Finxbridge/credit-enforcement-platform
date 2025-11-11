package com.finx.strategyengineservice.util;

import com.finx.strategyengineservice.domain.dto.CommonResponse;

/**
 * Utility class for wrapping responses
 */
public class ResponseWrapper {

    private ResponseWrapper() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.success(data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.success(message, data);
    }

    public static <T> CommonResponse<T> successMessage(String message) {
        return CommonResponse.successMessage(message);
    }

    public static <T> CommonResponse<T> failure(String message) {
        return CommonResponse.failure(message);
    }

    public static <T> CommonResponse<T> failure(String message, String errorCode) {
        return CommonResponse.failure(message, errorCode);
    }
}
