package com.finx.agencymanagement.exception;

import lombok.Getter;

/**
 * Base Exception for Agency Management Service
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Getter
public class BaseException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public BaseException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
        this.httpStatus = 500;
    }

    public BaseException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
