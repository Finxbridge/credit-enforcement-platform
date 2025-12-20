package com.finx.agencymanagement.exception;

/**
 * Business Exception for Agency Management Service
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super("BUSINESS_ERROR", message, 400);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message, 400);
    }
}
