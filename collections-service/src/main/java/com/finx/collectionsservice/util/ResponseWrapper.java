package com.finx.collectionsservice.util;

import com.finx.collectionsservice.domain.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ResponseWrapper {

    public static <T> ResponseEntity<CommonResponse<T>> ok(String message, T data) {
        CommonResponse<T> response = CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<CommonResponse<String>> okMessage(String message) {
        CommonResponse<String> response = CommonResponse.<String>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(String message, T data) {
        CommonResponse<T> response = CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public static <T> ResponseEntity<CommonResponse<T>> accepted(String message, T data) {
        CommonResponse<T> response = CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    public static <T> ResponseEntity<CommonResponse<T>> error(String message, HttpStatus status) {
        CommonResponse<T> response = CommonResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
