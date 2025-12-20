package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common Response Wrapper for all microservices
 *
 * @param <T> The type of payload
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private String status;
    private T payload;
    private String message;
    private String errorCode;
    private Object errors;

    public static <T> CommonResponse<T> success(T payload) {
        return CommonResponse.<T>builder()
                .status("success")
                .payload(payload)
                .build();
    }

    public static <T> CommonResponse<T> success(String message, T payload) {
        return CommonResponse.<T>builder()
                .status("success")
                .message(message)
                .payload(payload)
                .build();
    }

    public static <T> CommonResponse<T> successMessage(String message) {
        return CommonResponse.<T>builder()
                .status("success")
                .message(message)
                .payload(null)
                .build();
    }

    public static <T> CommonResponse<T> failure(String message) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .payload(null)
                .build();
    }

    public static <T> CommonResponse<T> failure(String message, String errorCode) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(errorCode)
                .payload(null)
                .build();
    }

    public static <T> CommonResponse<T> failure(String message, Object errors) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errors(errors)
                .payload(null)
                .build();
    }

    public static <T> CommonResponse<T> failure(String message, String errorCode, Object errors) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .payload(null)
                .build();
    }

    public static <T> CommonResponse<T> wrap(T response) {
        if (response == null) {
            return failure("No response data");
        }
        return success(response);
    }

    public static <T> CommonResponse<T> wrap(String message, T response) {
        if (response == null) {
            return failure(message != null ? message : "No response data");
        }
        return success(message, response);
    }

    public static <T> CommonResponse<T> fromException(Exception exception) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(exception.getMessage() != null ? exception.getMessage() : "An error occurred")
                .errorCode(exception.getClass().getSimpleName())
                .payload(null)
                .build();
    }

    public static <T> CommonResponse<T> fromException(String message, Exception exception) {
        return CommonResponse.<T>builder()
                .status("failure")
                .message(message)
                .errorCode(exception.getClass().getSimpleName())
                .payload(null)
                .build();
    }
}
