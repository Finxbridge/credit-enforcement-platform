package com.finx.casesourcingservice.domain.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation result for a single case CSV row
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseValidationResult {

    private Integer rowNumber;
    private String externalCaseId;
    private boolean isValid;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public void addError(String fieldName, String errorMessage) {
        errors.add(String.format("%s: %s", fieldName, errorMessage));
    }
}
