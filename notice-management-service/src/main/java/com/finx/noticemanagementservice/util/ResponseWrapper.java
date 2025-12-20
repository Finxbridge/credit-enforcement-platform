package com.finx.noticemanagementservice.util;

import com.finx.noticemanagementservice.domain.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseWrapper {

    private ResponseWrapper() {
    }

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.success("Success", data);
    }

    public static <T> CommonResponse<T> success(T data, String message) {
        return CommonResponse.success(message, data);
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(CommonResponse.success(message, data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> okMessage(String message) {
        return ResponseEntity.ok(CommonResponse.success(message));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(message, data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static <T> ResponseEntity<CommonResponse<T>> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(CommonResponse.error(message, errorCode));
    }
}
