package com.finx.agencymanagement.util;

import com.finx.agencymanagement.domain.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Response Wrapper Utility for Agency Management Service
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public class ResponseWrapper {

    private ResponseWrapper() {
        // Utility class - prevent instantiation
    }

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

    public static <T> ResponseEntity<CommonResponse<T>> accepted(String message, T payload) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success(message, payload));
    }

    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message));
    }

    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message, String errorCode) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message, errorCode));
    }

    public static <T> ResponseEntity<CommonResponse<T>> badRequest(String message, Object errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.failure(message, errors));
    }

    public static <T> ResponseEntity<CommonResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.failure(message, "UNAUTHORIZED"));
    }

    public static <T> ResponseEntity<CommonResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.failure(message, "FORBIDDEN"));
    }

    public static <T> ResponseEntity<CommonResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.failure(message, "NOT_FOUND"));
    }

    public static <T> ResponseEntity<CommonResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.failure(message, "INTERNAL_ERROR"));
    }

    public static <T> ResponseEntity<CommonResponse<T>> fromException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fromException(exception));
    }

    public static <T> ResponseEntity<CommonResponse<T>> fromException(String message, Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fromException(message, exception));
    }

    public static <T> ResponseEntity<CommonResponse<T>> wrap(T response) {
        return ResponseEntity.ok(CommonResponse.wrap(response));
    }

    public static <T> ResponseEntity<CommonResponse<T>> wrap(String message, T response) {
        return ResponseEntity.ok(CommonResponse.wrap(message, response));
    }

    @SuppressWarnings("null")
    public static <T> ResponseEntity<CommonResponse<T>> custom(HttpStatus status, CommonResponse<T> response) {
        return ResponseEntity.status(status).body(response);
    }
}
