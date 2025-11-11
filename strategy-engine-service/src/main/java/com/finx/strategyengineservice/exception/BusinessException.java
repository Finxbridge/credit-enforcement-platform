package com.finx.strategyengineservice.exception;

/**
 * Exception for business logic violations
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super("BUSINESS_ERROR", message, 400);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message, 400);
    }
}
