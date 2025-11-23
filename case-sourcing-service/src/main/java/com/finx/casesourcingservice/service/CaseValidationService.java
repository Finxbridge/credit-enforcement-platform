package com.finx.casesourcingservice.service;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.domain.dto.csv.CaseValidationResult;
import com.finx.casesourcingservice.repository.CaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating case CSV records
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseValidationService {

    private final CaseRepository caseRepository;

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final List<String> SUPPORTED_LANGUAGES = List.of("en", "hi", "ta", "te", "mr", "kn", "ml", "gu", "bn", "pa");

    /**
     * Validate a single CSV row
     */
    public CaseValidationResult validateCaseRow(CaseCsvRowDTO row) {
        CaseValidationResult result = CaseValidationResult.builder()
                .rowNumber(row.getRowNumber())
                .externalCaseId(row.getExternalCaseId())
                .isValid(true)
                .build();

        // Required field validations
        validateRequiredFields(row, result);

        // Format validations
        validateFormats(row, result);

        // Business rule validations
        validateBusinessRules(row, result);

        // Check for duplicates
        checkDuplicates(row, result);

        result.setValid(result.getErrors().isEmpty());
        return result;
    }

    private void validateRequiredFields(CaseCsvRowDTO row, CaseValidationResult result) {
        if (isNullOrEmpty(row.getExternalCaseId())) {
            result.addError("externalCaseId", "External Case ID is required");
        }

        if (isNullOrEmpty(row.getLoanAccountNumber())) {
            result.addError("loanAccountNumber", "Loan Account Number is required");
        }

        if (isNullOrEmpty(row.getCustomerCode())) {
            result.addError("customerCode", "Customer Code is required");
        }

        if (isNullOrEmpty(row.getFullName())) {
            result.addError("fullName", "Customer Full Name is required");
        }

        if (isNullOrEmpty(row.getMobileNumber())) {
            result.addError("mobileNumber", "Mobile Number is required");
        }

        if (isNullOrEmpty(row.getTotalOutstanding())) {
            result.addError("totalOutstanding", "Total Outstanding amount is required");
        }

        if (isNullOrEmpty(row.getDpd())) {
            result.addError("dpd", "DPD (Days Past Due) is required");
        }

        if (isNullOrEmpty(row.getGeographyCode())) {
            result.addError("geographyCode", "Geography Code is required");
        }

        if (isNullOrEmpty(row.getLanguage())) {
            result.addError("language", "Language is required");
        }
    }

    private void validateFormats(CaseCsvRowDTO row, CaseValidationResult result) {
        // Mobile number validation
        if (!isNullOrEmpty(row.getMobileNumber()) && !MOBILE_PATTERN.matcher(row.getMobileNumber()).matches()) {
            result.addError("mobileNumber", "Invalid mobile number format (should be 10 digits starting with 6-9)");
        }

        // Language validation
        if (!isNullOrEmpty(row.getLanguage()) && !SUPPORTED_LANGUAGES.contains(row.getLanguage().toLowerCase())) {
            result.addError("language", "Invalid language code. Supported: " + String.join(", ", SUPPORTED_LANGUAGES));
        }
    }

    private void validateBusinessRules(CaseCsvRowDTO row, CaseValidationResult result) {
        // Total outstanding should be positive
        if (!isNullOrEmpty(row.getTotalOutstanding())) {
            try {
                java.math.BigDecimal totalOutstanding = new java.math.BigDecimal(row.getTotalOutstanding());
                if (totalOutstanding.signum() <= 0) {
                    result.addError("totalOutstanding", "Total Outstanding must be greater than zero");
                }
            } catch (NumberFormatException e) {
                result.addError("totalOutstanding", "Invalid totalOutstanding format: " + row.getTotalOutstanding());
            }
        }

        // DPD should be non-negative
        if (!isNullOrEmpty(row.getDpd())) {
            try {
                Integer dpd = Integer.parseInt(row.getDpd());
                if (dpd < 0) {
                    result.addError("dpd", "DPD cannot be negative");
                }
            } catch (NumberFormatException e) {
                result.addError("dpd", "Invalid DPD format: " + row.getDpd());
            }
        }
    }

    private void checkDuplicates(CaseCsvRowDTO row, CaseValidationResult result) {
        // Check if case already exists by external case ID
        if (!isNullOrEmpty(row.getExternalCaseId())) {
            boolean exists = caseRepository.findByExternalCaseId(row.getExternalCaseId()).isPresent();
            if (exists) {
                result.addError("externalCaseId", "Duplicate case - External Case ID already exists");
            }
        }

        // Check if loan account number already exists
        // This would need a loan repository check - skipping for now as it's a soft reference
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
