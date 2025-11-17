package com.finx.casesourcingservice.service;

import com.finx.casesourcingservice.domain.dto.csv.CaseCsvRowDTO;
import com.finx.casesourcingservice.domain.dto.csv.CaseValidationResult;
import com.finx.casesourcingservice.repository.CaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    // RFC 5322 compliant email pattern (simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
    }

    private void validateFormats(CaseCsvRowDTO row, CaseValidationResult result) {
        // Mobile number validation
        if (!isNullOrEmpty(row.getMobileNumber()) && !MOBILE_PATTERN.matcher(row.getMobileNumber()).matches()) {
            result.addError("mobileNumber", "Invalid mobile number format (should be 10 digits starting with 6-9)");
        }

        // Alternative mobile number validation
        if (!isNullOrEmpty(row.getAlternateMobile()) && !MOBILE_PATTERN.matcher(row.getAlternateMobile()).matches()) {
            result.addError("alternateMobile", "Invalid alternative mobile number format (should be 10 digits starting with 6-9)");
        }

        // Email validation
        if (!isNullOrEmpty(row.getEmail()) && !EMAIL_PATTERN.matcher(row.getEmail()).matches()) {
            result.addError("email", "Invalid email format");
        }

        // Pincode validation
        if (!isNullOrEmpty(row.getPincode()) && !PINCODE_PATTERN.matcher(row.getPincode()).matches()) {
            result.addError("pincode", "Invalid pincode format (should be 6 digits)");
        }

        // Date validations
        if (!isNullOrEmpty(row.getDisbursementDate())) {
            try {
                LocalDate.parse(row.getDisbursementDate(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                result.addError("disbursementDate", "Invalid date format (expected: dd-MM-yyyy)");
            }
        }

        if (!isNullOrEmpty(row.getDueDate())) {
            try {
                LocalDate.parse(row.getDueDate(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                result.addError("dueDate", "Invalid date format (expected: dd-MM-yyyy)");
            }
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
                // Validate bucket based on DPD
                if (!isNullOrEmpty(row.getBucket())) {
                    validateBucket(dpd, row.getBucket(), result);
                }
            } catch (NumberFormatException e) {
                result.addError("dpd", "Invalid DPD format: " + row.getDpd());
            }
        }
    }

    private void validateBucket(Integer dpd, String bucket, CaseValidationResult result) {
        // Business rule: Bucket should match DPD range
        // Example: X (0-30), X1 (31-60), X2 (61-89), X3 (90-119), X4+ (120+)
        String expectedBucket;
        if (dpd <= 30) {
            expectedBucket = "X";
        } else if (dpd <= 60) {
            expectedBucket = "X1";
        } else if (dpd < 90) {
            expectedBucket = "X2";
        } else if (dpd < 120) {
            expectedBucket = "X3";
        } else {
            expectedBucket = "X4+";
        }

        if (!bucket.equals(expectedBucket)) {
            result.addError("bucket", String.format("Bucket '%s' doesn't match DPD %d (expected: %s)",
                    bucket, dpd, expectedBucket));
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
