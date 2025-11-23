package com.finx.strategyengineservice.exception;

/**
 * Exception for resource not found scenarios
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, 404);
    }

    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message, 404);
    }
}
