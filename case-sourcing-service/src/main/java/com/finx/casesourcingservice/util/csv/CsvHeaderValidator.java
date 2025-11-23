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
 */
@Slf4j
@Component
public class CsvHeaderValidator {

    // Expected headers for case upload CSV (required fields only)
    private static final List<String> CASE_UPLOAD_REQUIRED_HEADERS = List.of(
            "case_id",
            "loan_id",
            "customer_code",
            "customer_name",
            "phone",
            "geography",
            "language",
            "outstanding",
            "dpd"
    );

    // All headers are required now - keeping this for consistency with validation logic
    private static final List<String> CASE_UPLOAD_ALL_HEADERS = List.of(
            "case_id",
            "loan_id",
            "customer_code",
            "customer_name",
            "phone",
            "geography",
            "language",
            "outstanding",
            "dpd"
    );

    /**
     * Validate case upload CSV headers
     *
     * @param csvFile CSV file input stream
     * @return Validation result with missing/unknown headers and suggestions
     */
    public HeaderValidationResult validateCaseUploadHeaders(org.springframework.web.multipart.MultipartFile csvFile) {
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

            return validateHeaders(providedHeaders, CASE_UPLOAD_REQUIRED_HEADERS, CASE_UPLOAD_ALL_HEADERS);

        } catch (IOException e) {
            log.error("Error reading CSV file for header validation", e);
            return HeaderValidationResult.builder()
                    .isValid(false)
                    .message("Failed to read CSV file: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Validate headers against expected list
     */
    private HeaderValidationResult validateHeaders(List<String> providedHeaders,
                                                    List<String> requiredHeaders,
                                                    List<String> allExpectedHeaders) {

        // Find missing required headers
        List<String> missingHeaders = requiredHeaders.stream()
                .filter(required -> !providedHeaders.contains(required))
                .collect(Collectors.toList());

        // Find unknown headers (not in expected list)
        List<String> unknownHeaders = providedHeaders.stream()
                .filter(provided -> !allExpectedHeaders.contains(provided))
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
            message = "Missing " + missingHeaders.size() + " required header(s)";
        }

        return HeaderValidationResult.builder()
                .isValid(isValid)
                .message(message)
                .missingHeaders(missingHeaders)
                .unknownHeaders(unknownHeaders)
                .suggestions(suggestions)
                .expectedHeaders(allExpectedHeaders)
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
}
