package com.finx.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API Response DTO
 * Purpose: Standardized response wrapper for all API endpoints
 *
 * Response Format:
 * Success: { "status": "success", "payload": {...} }
 * Failure: { "status": "failure", "payload": null, "message": "error" }
 *
 * @version 2.0.0 - Updated to use "payload" field instead of "data"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {

    @Builder.Default
    private String status = "success"; // success, failure, warning

    private String message;

    @JsonProperty("payload")  // JSON will use "payload" but code uses "data"
    private T data;

    private String errorCode;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Object errors; // For validation errors

    // Success response factory methods
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .status("success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response factory methods (mapped to "failure" status)
    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .status("failure")
                .message(message)
                .data(null)  // Explicitly set payload to null
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDTO<T> error(String message, Object errors) {
        return ApiResponseDTO.<T>builder()
                .status("failure")
                .message(message)
                .errors(errors)
                .data(null)  // Explicitly set payload to null
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDTO<T> error(String message, String errorCode) {
        return ApiResponseDTO.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(errorCode)
                .data(null)  // Explicitly set payload to null
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Failure response factory methods (explicit)
    public static <T> ApiResponseDTO<T> failure(String message) {
        return ApiResponseDTO.<T>builder()
                .status("failure")
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDTO<T> failure(String message, String errorCode) {
        return ApiResponseDTO.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(errorCode)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Warning response factory method
    public static <T> ApiResponseDTO<T> warning(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .status("warning")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
