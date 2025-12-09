package com.finx.casesourcingservice.service;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.domain.dto.csv.CaseValidationResult;
import com.finx.casesourcingservice.repository.CaseRepository;
import com.finx.casesourcingservice.util.csv.CsvHeaderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating case CSV records
 * Updated to use unified CSV format field names
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseValidationService {

    private final CaseRepository caseRepository;

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final List<String> SUPPORTED_LANGUAGES = List.of("en", "hi", "ta", "te", "mr", "kn", "ml", "gu", "bn", "pa");

    /**
     * Validate a single CSV row for case sourcing
     */
    public CaseValidationResult validateCaseRow(CaseCsvRowDTO row) {
        return validateCaseRow(row, CsvHeaderValidator.ValidationType.CASE_SOURCING);
    }

    /**
     * Validate a single CSV row with specific validation type
     */
    public CaseValidationResult validateCaseRow(CaseCsvRowDTO row, CsvHeaderValidator.ValidationType validationType) {
        CaseValidationResult result = CaseValidationResult.builder()
                .rowNumber(row.getRowNumber())
                .externalCaseId(row.getAccountNo()) // Use ACCOUNT NO as external case ID
                .isValid(true)
                .build();

        // Required field validations based on unified CSV format
        validateRequiredFields(row, result, validationType);

        // Format validations
        validateFormats(row, result);

        // Business rule validations
        validateBusinessRules(row, result);

        // Check for duplicates
        checkDuplicates(row, result);

        result.setValid(result.getErrors().isEmpty());
        return result;
    }

    private void validateRequiredFields(CaseCsvRowDTO row, CaseValidationResult result, CsvHeaderValidator.ValidationType validationType) {
        // Mandatory fields for all types: CUSTOMER NAME, MOBILE NO, OVERDUE AMOUNT, EMI START DATE,
        // PRIMARY ADDRESS, CITY, STATE, PINCODE, PRODUCT, LOCATION, DPD, LANGUAGE

        if (isNullOrEmpty(row.getCustomerName())) {
            result.addError("customerName", "Customer Name is required");
        }

        if (isNullOrEmpty(row.getMobileNo())) {
            result.addError("mobileNo", "Mobile No is required");
        }

        if (isNullOrEmpty(row.getOverdueAmount())) {
            result.addError("overdueAmount", "Overdue Amount is required");
        }

        if (isNullOrEmpty(row.getEmiStartDate())) {
            result.addError("emiStartDate", "EMI Start Date is required");
        }

        if (isNullOrEmpty(row.getPrimaryAddress())) {
            result.addError("primaryAddress", "Primary Address is required");
        }

        if (isNullOrEmpty(row.getCity())) {
            result.addError("city", "City is required");
        }

        if (isNullOrEmpty(row.getState())) {
            result.addError("state", "State is required");
        }

        if (isNullOrEmpty(row.getPincode())) {
            result.addError("pincode", "Pincode is required");
        }

        if (isNullOrEmpty(row.getProduct())) {
            result.addError("product", "Product is required");
        }

        if (isNullOrEmpty(row.getLocation())) {
            result.addError("location", "Location is required");
        }

        if (isNullOrEmpty(row.getDpd())) {
            result.addError("dpd", "DPD (Days Past Due) is required");
        }

        if (isNullOrEmpty(row.getLanguage())) {
            result.addError("language", "Language is required");
        }

        // For Allocation/Reallocation: PRIMARY AGENT is mandatory
        if (validationType == CsvHeaderValidator.ValidationType.ALLOCATION ||
            validationType == CsvHeaderValidator.ValidationType.REALLOCATION) {
            if (isNullOrEmpty(row.getPrimaryAgent())) {
                result.addError("primaryAgent", "Primary Agent is required for " + validationType.name().toLowerCase());
            }
        }
    }

    private void validateFormats(CaseCsvRowDTO row, CaseValidationResult result) {
        // Mobile number validation
        if (!isNullOrEmpty(row.getMobileNo()) && !MOBILE_PATTERN.matcher(row.getMobileNo()).matches()) {
            result.addError("mobileNo", "Invalid mobile number format (should be 10 digits starting with 6-9)");
        }

        // Language validation
        if (!isNullOrEmpty(row.getLanguage()) && !SUPPORTED_LANGUAGES.contains(row.getLanguage().toLowerCase())) {
            result.addError("language", "Invalid language code. Supported: " + String.join(", ", SUPPORTED_LANGUAGES));
        }

        // Pincode validation (6 digits)
        if (!isNullOrEmpty(row.getPincode()) && !row.getPincode().matches("^\\d{6}$")) {
            result.addError("pincode", "Invalid pincode format (should be 6 digits)");
        }
    }

    private void validateBusinessRules(CaseCsvRowDTO row, CaseValidationResult result) {
        // Overdue amount should be positive
        if (!isNullOrEmpty(row.getOverdueAmount())) {
            try {
                java.math.BigDecimal overdueAmount = new java.math.BigDecimal(row.getOverdueAmount());
                if (overdueAmount.signum() <= 0) {
                    result.addError("overdueAmount", "Overdue Amount must be greater than zero");
                }
            } catch (NumberFormatException e) {
                result.addError("overdueAmount", "Invalid Overdue Amount format: " + row.getOverdueAmount());
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
        // Check if case already exists by account number (loan account number)
        if (!isNullOrEmpty(row.getAccountNo())) {
            // Note: Would need to check against loan_details.loan_account_number
            // For now, check if external case id exists
            boolean exists = caseRepository.findByExternalCaseId(row.getAccountNo()).isPresent();
            if (exists) {
                result.addError("accountNo", "Duplicate case - Account No already exists");
            }
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
