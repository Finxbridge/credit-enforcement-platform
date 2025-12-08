package com.finx.communication.util;

import com.finx.communication.exception.ValidationException;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public final class ValidationUtil {

    private ValidationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "^[6-9]\\d{9}$");

    private static final Pattern PAN_PATTERN = Pattern.compile(
            "^[A-Z]{5}[0-9]{4}[A-Z]$");

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate mobile number (Indian format)
     */
    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }

    /**
     * Validate PAN number
     */
    public static boolean isValidPAN(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan).matches();
    }

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if collection is null or empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Require non-null value
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName, "cannot be null");
        }
        return value;
    }

    /**
     * Require non-empty string
     */
    public static String requireNonEmpty(String value, String fieldName) {
        if (isEmpty(value)) {
            throw new ValidationException(fieldName, "cannot be empty");
        }
        return value;
    }
}
