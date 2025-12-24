package com.finx.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations with IST (Indian Standard Time) support.
 * All date/time operations in the application should use this utility to ensure
 * consistent timezone handling across the system.
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
public final class DateTimeUtil {

    private DateTimeUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Indian Standard Time Zone ID (UTC+5:30)
     */
    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    /**
     * UTC Zone ID
     */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * Standard date format used across the application
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Standard date-time format used across the application
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Standard date-time format with milliseconds
     */
    public static final String DATETIME_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * Display format for date
     */
    public static final String DISPLAY_DATE_FORMAT = "dd-MM-yyyy";

    /**
     * Display format for date-time
     */
    public static final String DISPLAY_DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    /**
     * DateTimeFormatter for standard date format
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * DateTimeFormatter for standard date-time format
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    /**
     * DateTimeFormatter for date-time with milliseconds
     */
    public static final DateTimeFormatter DATETIME_WITH_MILLIS_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT_WITH_MILLIS);

    /**
     * DateTimeFormatter for display date format
     */
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT);

    /**
     * DateTimeFormatter for display date-time format
     */
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATETIME_FORMAT);

    // ==================== Current Date/Time Methods ====================

    /**
     * Get current date-time in IST timezone
     *
     * @return Current LocalDateTime in IST
     */
    public static LocalDateTime nowInIST() {
        return LocalDateTime.now(IST_ZONE);
    }

    /**
     * Get current date in IST timezone
     *
     * @return Current LocalDate in IST
     */
    public static LocalDate todayInIST() {
        return LocalDate.now(IST_ZONE);
    }

    /**
     * Get current time in IST timezone
     *
     * @return Current LocalTime in IST
     */
    public static LocalTime currentTimeInIST() {
        return LocalTime.now(IST_ZONE);
    }

    /**
     * Get current ZonedDateTime in IST timezone
     *
     * @return Current ZonedDateTime in IST
     */
    public static ZonedDateTime zonedNowInIST() {
        return ZonedDateTime.now(IST_ZONE);
    }

    // ==================== Conversion Methods ====================

    /**
     * Convert UTC LocalDateTime to IST LocalDateTime
     *
     * @param utcDateTime LocalDateTime in UTC
     * @return LocalDateTime in IST
     */
    public static LocalDateTime utcToIST(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        ZonedDateTime utcZoned = utcDateTime.atZone(UTC_ZONE);
        return utcZoned.withZoneSameInstant(IST_ZONE).toLocalDateTime();
    }

    /**
     * Convert IST LocalDateTime to UTC LocalDateTime
     *
     * @param istDateTime LocalDateTime in IST
     * @return LocalDateTime in UTC
     */
    public static LocalDateTime istToUTC(LocalDateTime istDateTime) {
        if (istDateTime == null) {
            return null;
        }
        ZonedDateTime istZoned = istDateTime.atZone(IST_ZONE);
        return istZoned.withZoneSameInstant(UTC_ZONE).toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to ZonedDateTime in IST
     *
     * @param localDateTime LocalDateTime to convert
     * @return ZonedDateTime in IST
     */
    public static ZonedDateTime toZonedIST(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(IST_ZONE);
    }

    /**
     * Convert epoch milliseconds to LocalDateTime in IST
     *
     * @param epochMillis Epoch milliseconds
     * @return LocalDateTime in IST
     */
    public static LocalDateTime fromEpochMillisToIST(long epochMillis) {
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(epochMillis),
                IST_ZONE
        );
    }

    /**
     * Convert LocalDateTime in IST to epoch milliseconds
     *
     * @param istDateTime LocalDateTime in IST
     * @return Epoch milliseconds
     */
    public static long toEpochMillis(LocalDateTime istDateTime) {
        if (istDateTime == null) {
            return 0;
        }
        return istDateTime.atZone(IST_ZONE).toInstant().toEpochMilli();
    }

    // ==================== Formatting Methods ====================

    /**
     * Format LocalDateTime to standard format string
     *
     * @param dateTime LocalDateTime to format
     * @return Formatted string
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format LocalDateTime with custom pattern
     *
     * @param dateTime LocalDateTime to format
     * @param pattern Custom pattern
     * @return Formatted string
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Format LocalDate to standard format string
     *
     * @param date LocalDate to format
     * @return Formatted string
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime for display (dd-MM-yyyy HH:mm:ss)
     *
     * @param dateTime LocalDateTime to format
     * @return Formatted string for display
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    /**
     * Format LocalDate for display (dd-MM-yyyy)
     *
     * @param date LocalDate to format
     * @return Formatted string for display
     */
    public static String formatForDisplay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    // ==================== Parsing Methods ====================

    /**
     * Parse string to LocalDateTime using standard format
     *
     * @param dateTimeString String to parse
     * @return Parsed LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
    }

    /**
     * Parse string to LocalDateTime using custom pattern
     *
     * @param dateTimeString String to parse
     * @param pattern Custom pattern
     * @return Parsed LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString, String pattern) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse string to LocalDate using standard format
     *
     * @param dateString String to parse
     * @return Parsed LocalDate
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }

    // ==================== Utility Methods ====================

    /**
     * Get start of day in IST for given date
     *
     * @param date LocalDate
     * @return LocalDateTime at start of day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Get end of day in IST for given date
     *
     * @param date LocalDate
     * @return LocalDateTime at end of day (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Get start of current day in IST
     *
     * @return LocalDateTime at start of today in IST
     */
    public static LocalDateTime startOfTodayInIST() {
        return todayInIST().atStartOfDay();
    }

    /**
     * Get end of current day in IST
     *
     * @return LocalDateTime at end of today in IST
     */
    public static LocalDateTime endOfTodayInIST() {
        return todayInIST().atTime(LocalTime.MAX);
    }

    /**
     * Check if given date-time is in past (compared to IST now)
     *
     * @param dateTime LocalDateTime to check
     * @return true if date-time is in past
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(nowInIST());
    }

    /**
     * Check if given date-time is in future (compared to IST now)
     *
     * @param dateTime LocalDateTime to check
     * @return true if date-time is in future
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(nowInIST());
    }

    /**
     * Check if given date is today in IST
     *
     * @param date LocalDate to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(todayInIST());
    }

    /**
     * Calculate days between two dates
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days between dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate hours between two date-times
     *
     * @param startDateTime Start date-time
     * @param endDateTime End date-time
     * @return Number of hours between date-times
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * Add days to given date
     *
     * @param date LocalDate
     * @param days Number of days to add
     * @return New LocalDate with days added
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * Add hours to given date-time
     *
     * @param dateTime LocalDateTime
     * @param hours Number of hours to add
     * @return New LocalDateTime with hours added
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }
}
