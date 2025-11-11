package com.finx.allocationreallocationservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private String status;
    private T payload;
    private String message;
    private String errorCode;
    private Map<String, Object> errors;

    private CommonResponse(String status, T payload, String message, String errorCode, Map<String, Object> errors) {
        this.status = status;
        this.payload = payload;
        this.message = message;
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public static <T> CommonResponse<T> success(T payload) {
        return new CommonResponse<>("success", payload, null, null, null);
    }

    public static <T> CommonResponse<T> success(String message, T payload) {
        return new CommonResponse<>("success", payload, message, null, null);
    }

    public static <T> CommonResponse<T> successMessage(String message) {
        return new CommonResponse<>("success", null, message, null, null);
    }

    public static <T> CommonResponse<T> failure(String message) {
        return new CommonResponse<>("failure", null, message, null, null);
    }

    public static <T> CommonResponse<T> failure(String message, String errorCode) {
        return new CommonResponse<>("failure", null, message, errorCode, null);
    }

    public static <T> CommonResponse<T> failure(String message, Map<String, Object> errors) {
        return new CommonResponse<>("failure", null, message, null, errors);
    }

    public static <T> CommonResponse<T> failure(String message, String errorCode, Map<String, Object> errors) {
        return new CommonResponse<>("failure", null, message, errorCode, errors);
    }

    public static <T> CommonResponse<T> fromException(Exception exception) {
        return new CommonResponse<>("failure", null, exception.getMessage() != null ? exception.getMessage() : "An error occurred", exception.getClass().getSimpleName(), null);
    }

    public static <T> CommonResponse<T> fromException(String message, Exception exception) {
        return new CommonResponse<>("failure", null, message, exception.getClass().getSimpleName(), null);
    }
}
