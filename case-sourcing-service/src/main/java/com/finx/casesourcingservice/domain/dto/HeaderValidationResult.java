package com.finx.casesourcingservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for CSV header validation result
 * Returned before file upload to validate headers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderValidationResult {

    private Boolean isValid;
    private String message;

    // Missing required headers
    private List<String> missingHeaders;

    // Unknown/extra headers (possible typos)
    private List<String> unknownHeaders;

    // Suggestions for typos (fuzzy matching)
    private List<HeaderSuggestion> suggestions;

    // Expected headers list
    private List<String> expectedHeaders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeaderSuggestion {
        private String providedHeader;
        private String suggestedHeader;
        private Integer similarityScore; // 0-100
    }
}
