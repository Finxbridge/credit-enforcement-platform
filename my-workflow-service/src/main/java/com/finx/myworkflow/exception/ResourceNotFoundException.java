package com.finx.myworkflow.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with ID: " + id, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " not found with " + fieldName + ": " + fieldValue, "RESOURCE_NOT_FOUND");
    }
}
