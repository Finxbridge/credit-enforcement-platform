package com.finx.myworkflow.util;

import com.finx.myworkflow.domain.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseWrapper {

    private ResponseWrapper() {}

    public static <T> ResponseEntity<CommonResponse<T>> ok(T payload) {
        return ResponseEntity.ok(CommonResponse.success(payload));
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(String message, T payload) {
        return ResponseEntity.ok(CommonResponse.success(message, payload));
    }

    public static <T> ResponseEntity<CommonResponse<T>> okMessage(String message) {
        return ResponseEntity.ok(CommonResponse.successMessage(message));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(T payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Resource created successfully", payload));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(String message, T payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(message, payload));
    }

    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message));
    }

    public static <T> ResponseEntity<CommonResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.failure(message, "NOT_FOUND"));
    }

    public static <T> ResponseEntity<CommonResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(message, "INTERNAL_ERROR"));
    }
}
