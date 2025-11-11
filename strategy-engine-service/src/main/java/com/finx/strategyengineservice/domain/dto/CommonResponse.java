package com.finx.strategyengineservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Common Response Wrapper for all microservices
 * Wraps existing response beans into a standardized structure
 *
 * Response Structure:
 * Success: { "status": "success", "payload": {...} }
 * Failure: { "status": "failure", "payload": null, "message": "error message" }
 *
 * @param <T> The type of payload (can be existing response beans)
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    /**
     * Response status: "success" or "failure"
     */
    private String status;

    /**
     * Response payload - contains the actual data
     * For success: contains the response data/bean
     * For failure: null
     */
    private T payload;

    /**
     * Optional message for additional context
     */
    private String message;

    /**
     * Optional error code for failures
     */
    private String errorCode;

    /**
     * Optional error details (for validation errors, etc.)
     */
    private Map<String, Object> errors;

    // ============================================
    // SUCCESS RESPONSE FACTORY METHODS
    // ============================================

    /**
     * Create success response with payload only
     * Usage: CommonResponse.success(responseBean)
     */
    public static <T> CommonResponse<T> success(T payload) {
        return CommonResponse.<T>builder()
                .status("success")
                .payload(payload)
                .build();
    }

    /**
     * Create success response with message and payload
     * Usage: CommonResponse.success("Operation successful", responseBean)
     */
    public static <T> CommonResponse<T> success(String message, T payload) {
        return CommonResponse.<T>builder()
                .status("success")
                .message(message)
                .payload(payload)
                .build();
    }

    /**
     * Create success response with only message (no payload)
     * Usage: CommonResponse.successMessage("User deleted successfully")
     */
    public static <T> CommonResponse<T> successMessage(String message) {
        return CommonResponse.<T>builder()
                .status("success")
                .message(message)
                .payload(null)
                .build();
    }

    // ============================================
    // FAILURE RESPONSE FACTORY METHODS
    // ============================================

    /**
     * Create failure response with message only
     * Usage: CommonResponse.failure("User not found")
     */
    public static <T> CommonResponse<T> failure(String message) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .payload(null)
                .build();
    }

    /**
     * Create failure response with message and error code
     * Usage: CommonResponse.failure("Invalid credentials", "AUTH_001")
     */
    public static <T> CommonResponse<T> failure(String message, String errorCode) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(errorCode)
                .payload(null)
                .build();
    }

    /**
     * Create failure response with message and validation errors
     * Usage: CommonResponse.failure("Validation failed", validationErrors)
     */
    public static <T> CommonResponse<T> failure(String message, Map<String, Object> errors) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errors(errors)
                .payload(null)
                .build();
    }

    /**
     * Create failure response with all error details
     * Usage: CommonResponse.failure("Validation failed", "VAL_001",
     * validationErrors)
     */
    public static <T> CommonResponse<T> failure(String message, String errorCode, Map<String, Object> errors) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .payload(null)
                .build();
    }

    // ============================================
    // WRAPPER METHODS FOR EXISTING RESPONSES
    // ============================================

    /**
     * Wrap an existing response into CommonResponse
     * Automatically determines success/failure based on response type
     * Usage: CommonResponse.wrap(existingApiResponse)
     */
    public static <T> CommonResponse<T> wrap(T response) {
        if (response == null) {
            return failure("No response data");
        }
        return success(response);
    }

    /**
     * Wrap an existing response with custom message
     * Usage: CommonResponse.wrap("Operation completed", existingApiResponse)
     */
    public static <T> CommonResponse<T> wrap(String message, T response) {
        if (response == null) {
            return failure(message != null ? message : "No response data");
        }
        return success(message, response);
    }

    // ============================================
    // EXCEPTION WRAPPING
    // ============================================

    /**
     * Create failure response from exception
     * Usage: CommonResponse.fromException(exception)
     */
    public static <T> CommonResponse<T> fromException(Exception exception) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(exception.getMessage() != null ? exception.getMessage() : "An error occurred")
                .errorCode(exception.getClass().getSimpleName())
                .payload(null)
                .build();
    }

    /**
     * Create failure response from exception with custom message
     * Usage: CommonResponse.fromException("Failed to process request", exception)
     */
    public static <T> CommonResponse<T> fromException(String message, Exception exception) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(exception.getClass().getSimpleName())
                .payload(null)
                .build();
    }
}
