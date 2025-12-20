package com.finx.myworkflow.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private Object errors;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

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

    public static <T> CommonResponse<T> failure(String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> CommonResponse<T> failure(String message, String errorCode) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    public static <T> CommonResponse<T> failure(String message, Object errors) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }

    public static <T> CommonResponse<T> fromException(Exception e) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(e.getMessage())
                .errorCode("INTERNAL_ERROR")
                .build();
    }

    public static <T> CommonResponse<T> fromException(String message, Exception e) {
        return CommonResponse.<T>builder()
                .success(false)
                .message(message + ": " + e.getMessage())
                .errorCode("INTERNAL_ERROR")
                .build();
    }

    public static <T> CommonResponse<T> wrap(T response) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(response)
                .build();
    }

    public static <T> CommonResponse<T> wrap(String message, T response) {
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(response)
                .build();
    }
}
