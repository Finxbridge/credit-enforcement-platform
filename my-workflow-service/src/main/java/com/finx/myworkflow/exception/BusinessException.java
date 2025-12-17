package com.finx.myworkflow.exception;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR");
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode);
    }
}
