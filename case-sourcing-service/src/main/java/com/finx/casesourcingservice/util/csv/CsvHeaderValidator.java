package com.finx.casesourcingservice.util.csv;

import com.finx.casesourcingservice.domain.dto.HeaderValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for validating CSV headers before upload
 * Provides early feedback on header errors
 *
 * Mandatory Fields for Case Sourcing:
 * - CUSTOMER NAME, MOBILE NO, OVERDUE AMOUNT, EMI START DATE
 * - PRIMARY ADDRESS, CITY, STATE, PINCODE
 * - PRODUCT, LOCATION, DPD, LANGUAGE
 *
 * For Allocation/Reallocation: PRIMARY AGENT is also mandatory
 */
@Slf4j
@Component
public class CsvHeaderValidator {

    // Mandatory headers for case sourcing
    private static final List<String> CASE_SOURCING_REQUIRED_HEADERS = List.of(
            "CUSTOMER NAME",
            "MOBILE NO",
            "OVERDUE AMOUNT",
            "EMI START DATE",
            "PRIMARY ADDRESS",
            "CITY",
            "STATE",
            "PINCODE",
            "PRODUCT",
            "LOCATION",
            "DPD",
            "LANGUAGE"
    );

    // Additional mandatory for allocation/reallocation
    private static final List<String> ALLOCATION_ADDITIONAL_REQUIRED = List.of(
            "PRIMARY AGENT"
    );

    // All supported headers from the unified CSV format
    private static final List<String> ALL_SUPPORTED_HEADERS = List.of(
            // Lender & Account
            "LENDER", "ACCOUNT NO",
            // Customer Information
            "CUSTOMER NAME", "MOBILE NO", "CUSTOMER ID", "EMAIL",
            "SECONDARY MOBILE NUMBER", "RESI PHONE", "ADDITIONAL PHONE 2",
            // Address
            "PRIMARY ADDRESS", "SECONDARY ADDRESS", "CITY", "STATE", "PINCODE",
            // Financial Details
            "OVERDUE AMOUNT", "POS", "TOS", "LOAN AMOUNT OR LIMIT",
            "EMI AMOUNT", "PENALTY AMOUNT", "CHARGES", "OD INTEREST",
            // Overdue Breakdown
            "PRINCIPAL OVERDUE", "INTEREST OVERDUE", "FEES OVERDUE", "PENALTY OVERDUE",
            // EMI Details
            "EMI START DATE", "NO OF PAID EMI", "NO OF PENDING EMI",
            "Emi Overdue From", "Next EMI Date",
            // Loan Tenure
            "LOAN DURATION", "ROI",
            // Important Dates
            "DATE OF DISBURSEMENT", "MATURITY DATE", "DUE DATE", "WRITEOFF DATE",
            // DPD & Bucket
            "DPD", "RISK BUCKET", "SOM BUCKET", "SOM DPD", "CYCLE DUE",
            // Product & Scheme
            "PRODUCT", "SCHEME CODE", "PRODUCT SOURCING TYPE",
            // Credit Card Specific
            "MINIMUM AMOUNT DUE", "CARD OUTSTANDING", "STATEMENT DATE",
            "STATEMENT MONTH", "CARD STATUS", "LAST BILLED AMOUNT", "LAST 4 DIGITS",
            // Payment Information
            "LAST PAYMENT DATE", "LAST PAYMENT MODE", "LAST PAID AMOUNT",
            // Repayment Bank Details
            "BENEFICIARY ACCOUNT Number", "BENEFICIARY ACCOUNT NAME",
            "REPAYMENT BANK NAME", "REPAYMENT IFSC CODE", "REFERENCE URL",
            // Lender References
            "REFERENCE LENDER", "CO LENDER",
            // Family & Employment
            "FATHER SPOUSE NAME", "EMPLOYER OR BUSINESS ENTITY",
            // References
            "REFERENCE 1 NAME", "REFERENCE 1 NUMBER",
            "REFERENCE 2 NAME", "REFERENCE 2 NUMBER",
            // Block Status
            "BLOCK 1", "BLOCK 1 DATE", "BLOCK 2", "BLOCK 2 DATE",
            // Location & Geography
            "LOCATION", "ZONE", "LANGUAGE",
            // Agent Allocation
            "PRIMARY AGENT", "SECONDARY AGENT",
            // Sourcing
            "SOURCING RM NAME",
            // Flags
            "REVIEW FLAG",
            // Asset Details
            "ASSET DETAILS", "VEHICLE REGISTRATION NUMBER",
            "VEHICLE IDENTIFICATION NUMBER", "CHASSIS NUMBER",
            "MODEL MAKE", "BATTERY ID",
            // Dealer
            "DEALER NAME", "DEALER ADDRESS",
            // Agency
            "AGENCY NAME",
            // Export/Batch Specific
            "STATUS", "REMARKS"
    );

    public enum ValidationType {
        CASE_SOURCING,
        ALLOCATION,
        REALLOCATION
    }

    /**
     * Validate case upload CSV headers for case sourcing
     */
    public HeaderValidationResult validateCaseUploadHeaders(org.springframework.web.multipart.MultipartFile csvFile) {
        return validateHeaders(csvFile, ValidationType.CASE_SOURCING);
    }

    /**
     * Validate CSV headers for allocation
     */
    public HeaderValidationResult validateAllocationHeaders(org.springframework.web.multipart.MultipartFile csvFile) {
        return validateHeaders(csvFile, ValidationType.ALLOCATION);
    }

    /**
     * Validate CSV headers for reallocation
     */
    public HeaderValidationResult validateReallocationHeaders(org.springframework.web.multipart.MultipartFile csvFile) {
        return validateHeaders(csvFile, ValidationType.REALLOCATION);
    }

    /**
     * Validate CSV headers based on validation type
     */
    public HeaderValidationResult validateHeaders(org.springframework.web.multipart.MultipartFile csvFile,
                                                   ValidationType type) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {

            // Read first line (header row)
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                return HeaderValidationResult.builder()
                        .isValid(false)
                        .message("CSV file is empty or has no header row")
                        .build();
            }

            // Parse headers
            List<String> providedHeaders = Arrays.stream(headerLine.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            // Get required headers based on validation type
            List<String> requiredHeaders = getRequiredHeaders(type);

            return validateHeaders(providedHeaders, requiredHeaders, ALL_SUPPORTED_HEADERS);

        } catch (IOException e) {
            log.error("Error reading CSV file for header validation", e);
            return HeaderValidationResult.builder()
                    .isValid(false)
                    .message("Failed to read CSV file: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get required headers based on validation type
     */
    private List<String> getRequiredHeaders(ValidationType type) {
        List<String> required = new ArrayList<>(CASE_SOURCING_REQUIRED_HEADERS);

        if (type == ValidationType.ALLOCATION || type == ValidationType.REALLOCATION) {
            required.addAll(ALLOCATION_ADDITIONAL_REQUIRED);
        }

        return required;
    }

    /**
     * Validate headers against expected list
     */
    private HeaderValidationResult validateHeaders(List<String> providedHeaders,
                                                    List<String> requiredHeaders,
                                                    List<String> allExpectedHeaders) {

        // Normalize provided headers for comparison
        Set<String> normalizedProvided = providedHeaders.stream()
                .map(String::toUpperCase)
                .map(String::trim)
                .collect(Collectors.toSet());

        // Find missing required headers
        List<String> missingHeaders = requiredHeaders.stream()
                .filter(required -> !normalizedProvided.contains(required.toUpperCase()))
                .collect(Collectors.toList());

        // Find unknown headers (not in expected list)
        Set<String> normalizedExpected = allExpectedHeaders.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        List<String> unknownHeaders = providedHeaders.stream()
                .filter(provided -> !normalizedExpected.contains(provided.toUpperCase().trim()))
                .collect(Collectors.toList());

        // Generate suggestions for unknown headers (fuzzy matching)
        List<HeaderValidationResult.HeaderSuggestion> suggestions = new ArrayList<>();
        for (String unknown : unknownHeaders) {
            String bestMatch = findBestMatch(unknown, allExpectedHeaders);
            if (bestMatch != null) {
                int similarity = calculateSimilarity(unknown, bestMatch);
                if (similarity > 50) { // Only suggest if >50% similar
                    suggestions.add(HeaderValidationResult.HeaderSuggestion.builder()
                            .providedHeader(unknown)
                            .suggestedHeader(bestMatch)
                            .similarityScore(similarity)
                            .build());
                }
            }
        }

        boolean isValid = missingHeaders.isEmpty();

        String message;
        if (isValid) {
            if (unknownHeaders.isEmpty()) {
                message = "All headers are valid";
            } else {
                message = "Required headers present, but found " + unknownHeaders.size() + " unknown header(s)";
            }
        } else {
            message = "Missing " + missingHeaders.size() + " required header(s): " +
                    String.join(", ", missingHeaders);
        }

        return HeaderValidationResult.builder()
                .isValid(isValid)
                .message(message)
                .missingHeaders(missingHeaders)
                .unknownHeaders(unknownHeaders)
                .suggestions(suggestions)
                .expectedHeaders(requiredHeaders)
                .build();
    }

    /**
     * Find best matching header from expected list
     */
    private String findBestMatch(String provided, List<String> expectedHeaders) {
        return expectedHeaders.stream()
                .max(Comparator.comparingInt(expected -> calculateSimilarity(provided, expected)))
                .orElse(null);
    }

    /**
     * Calculate similarity score between two strings using Levenshtein distance
     * Returns 0-100 (100 = exact match)
     */
    private int calculateSimilarity(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        if (s1.equals(s2)) {
            return 100;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());

        if (maxLen == 0) {
            return 100;
        }

        return Math.max(0, 100 - (distance * 100 / maxLen));
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Get all supported headers for sample/template generation
     */
    public List<String> getAllSupportedHeaders() {
        return new ArrayList<>(ALL_SUPPORTED_HEADERS);
    }

    /**
     * Get required headers for case sourcing
     */
    public List<String> getCaseSourcingRequiredHeaders() {
        return new ArrayList<>(CASE_SOURCING_REQUIRED_HEADERS);
    }

    /**
     * Get required headers for allocation
     */
    public List<String> getAllocationRequiredHeaders() {
        List<String> required = new ArrayList<>(CASE_SOURCING_REQUIRED_HEADERS);
        required.addAll(ALLOCATION_ADDITIONAL_REQUIRED);
        return required;
    }
}
