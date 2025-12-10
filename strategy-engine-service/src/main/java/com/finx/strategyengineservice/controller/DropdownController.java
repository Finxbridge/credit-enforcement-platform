package com.finx.strategyengineservice.controller;

import com.finx.strategyengineservice.domain.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller to provide dropdown data for Strategy Engine frontend
 *
 * NOTE: For Template dropdowns, frontend should call Template Management Service directly:
 *   - GET /api/v1/templates/dropdown
 *   - GET /api/v1/templates/dropdown/{channel}
 *
 * This controller provides Strategy Engine specific dropdowns like:
 *   - Channel types
 *   - Schedule frequencies
 *   - Filter operators
 *   - Strategy statuses
 */
@RestController
@RequestMapping("/strategies/dropdowns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strategy Dropdowns", description = "APIs to get dropdown data for strategy configuration")
public class DropdownController {

    // ==================== Channel Types ====================

    @GetMapping("/channels")
    @Operation(
        summary = "Get available channel types",
        description = "Returns list of available communication channels for strategy actions"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getChannelTypes() {
        log.info("GET /strategies/dropdowns/channels - Fetching channel types");

        List<Map<String, String>> channels = Arrays.asList(
            Map.of("code", "SMS", "name", "SMS", "description", "Short Message Service"),
            Map.of("code", "WHATSAPP", "name", "WhatsApp", "description", "WhatsApp Messaging"),
            Map.of("code", "EMAIL", "name", "Email", "description", "Email Communication"),
            Map.of("code", "IVR", "name", "IVR", "description", "Interactive Voice Response"),
            Map.of("code", "NOTICE", "name", "Notice", "description", "Legal/Collection Notice")
        );

        return ResponseEntity.ok(CommonResponse.success("Channel types retrieved successfully", channels));
    }

    // ==================== Schedule Frequencies ====================

    @GetMapping("/frequencies")
    @Operation(
        summary = "Get available schedule frequencies",
        description = "Returns list of available schedule frequencies for strategy automation"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getScheduleFrequencies() {
        log.info("GET /strategies/dropdowns/frequencies - Fetching schedule frequencies");

        List<Map<String, String>> frequencies = Arrays.asList(
            Map.of("code", "DAILY", "name", "Daily", "description", "Execute every day"),
            Map.of("code", "WEEKLY", "name", "Weekly", "description", "Execute on specific days of week"),
            Map.of("code", "MONTHLY", "name", "Monthly", "description", "Execute on specific day of month")
        );

        return ResponseEntity.ok(CommonResponse.success("Schedule frequencies retrieved successfully", frequencies));
    }

    // ==================== Strategy Statuses ====================

    @GetMapping("/statuses")
    @Operation(
        summary = "Get available strategy statuses",
        description = "Returns list of available strategy statuses"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getStrategyStatuses() {
        log.info("GET /strategies/dropdowns/statuses - Fetching strategy statuses");

        List<Map<String, String>> statuses = Arrays.asList(
            Map.of("code", "DRAFT", "name", "Draft", "description", "Strategy is in draft mode"),
            Map.of("code", "ACTIVE", "name", "Active", "description", "Strategy is active and can be executed"),
            Map.of("code", "INACTIVE", "name", "Inactive", "description", "Strategy is disabled")
        );

        return ResponseEntity.ok(CommonResponse.success("Strategy statuses retrieved successfully", statuses));
    }

    // ==================== Filter Operators ====================

    @GetMapping("/operators/numeric")
    @Operation(
        summary = "Get numeric filter operators",
        description = "Returns list of available operators for numeric filters"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getNumericOperators() {
        log.info("GET /strategies/dropdowns/operators/numeric - Fetching numeric operators");

        List<Map<String, String>> operators = Arrays.asList(
            Map.of("code", ">=", "name", "Greater Than or Equal", "description", "Value >= specified"),
            Map.of("code", "<=", "name", "Less Than or Equal", "description", "Value <= specified"),
            Map.of("code", "=", "name", "Equal", "description", "Value equals specified"),
            Map.of("code", ">", "name", "Greater Than", "description", "Value > specified"),
            Map.of("code", "<", "name", "Less Than", "description", "Value < specified"),
            Map.of("code", "RANGE", "name", "Range", "description", "Value between two values")
        );

        return ResponseEntity.ok(CommonResponse.success("Numeric operators retrieved successfully", operators));
    }

    @GetMapping("/operators/date")
    @Operation(
        summary = "Get date filter operators",
        description = "Returns list of available operators for date filters"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getDateOperators() {
        log.info("GET /strategies/dropdowns/operators/date - Fetching date operators");

        List<Map<String, String>> operators = Arrays.asList(
            Map.of("code", ">=", "name", "On or After", "description", "Date >= specified"),
            Map.of("code", "<=", "name", "On or Before", "description", "Date <= specified"),
            Map.of("code", "=", "name", "Equal", "description", "Date equals specified"),
            Map.of("code", ">", "name", "After", "description", "Date > specified"),
            Map.of("code", "<", "name", "Before", "description", "Date < specified"),
            Map.of("code", "BETWEEN", "name", "Between", "description", "Date between two dates")
        );

        return ResponseEntity.ok(CommonResponse.success("Date operators retrieved successfully", operators));
    }

    @GetMapping("/operators/text")
    @Operation(
        summary = "Get text filter operators",
        description = "Returns list of available operators for text filters"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getTextOperators() {
        log.info("GET /strategies/dropdowns/operators/text - Fetching text operators");

        List<Map<String, String>> operators = Arrays.asList(
            Map.of("code", "IN", "name", "In", "description", "Value is in the specified list")
        );

        return ResponseEntity.ok(CommonResponse.success("Text operators retrieved successfully", operators));
    }

    // ==================== Filter Fields ====================

    @GetMapping("/fields/numeric")
    @Operation(
        summary = "Get available numeric filter fields",
        description = "Returns list of available numeric fields for filtering"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getNumericFields() {
        log.info("GET /strategies/dropdowns/fields/numeric - Fetching numeric fields");

        List<Map<String, String>> fields = Arrays.asList(
            Map.of("code", "DPD", "name", "Days Past Due", "description", "Days past due date"),
            Map.of("code", "OVERDUE_AMOUNT", "name", "Overdue Amount", "description", "Total overdue amount"),
            Map.of("code", "LOAN_AMOUNT", "name", "Loan Amount", "description", "Original loan amount"),
            Map.of("code", "EMI_AMOUNT", "name", "EMI Amount", "description", "Monthly EMI amount"),
            Map.of("code", "PAID_EMI", "name", "Paid EMI", "description", "Number of EMIs paid"),
            Map.of("code", "PENDING_EMI", "name", "Pending EMI", "description", "Number of pending EMIs"),
            Map.of("code", "POS", "name", "Principal Outstanding", "description", "Principal outstanding amount"),
            Map.of("code", "TOS", "name", "Total Outstanding", "description", "Total outstanding amount"),
            Map.of("code", "PENALTY_AMOUNT", "name", "Penalty Amount", "description", "Penalty amount"),
            Map.of("code", "LATE_FEES", "name", "Late Fees", "description", "Late fees amount"),
            Map.of("code", "OD_INTEREST", "name", "OD Interest", "description", "Overdue interest"),
            Map.of("code", "BUREAU_SCORE", "name", "Bureau Score", "description", "Credit bureau score")
        );

        return ResponseEntity.ok(CommonResponse.success("Numeric fields retrieved successfully", fields));
    }

    @GetMapping("/fields/text")
    @Operation(
        summary = "Get available text filter fields",
        description = "Returns list of available text fields for filtering"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getTextFields() {
        log.info("GET /strategies/dropdowns/fields/text - Fetching text fields");

        List<Map<String, String>> fields = Arrays.asList(
            Map.of("code", "LANGUAGE", "name", "Language", "description", "Customer preferred language"),
            Map.of("code", "STATE", "name", "State", "description", "Customer state"),
            Map.of("code", "CITY", "name", "City", "description", "Customer city"),
            Map.of("code", "PINCODE", "name", "Pincode", "description", "Customer pincode"),
            Map.of("code", "STATUS", "name", "Case Status", "description", "Current case status"),
            Map.of("code", "SOURCE_TYPE", "name", "Source Type", "description", "Case source type"),
            Map.of("code", "OWNERSHIP", "name", "Ownership", "description", "Case ownership")
        );

        return ResponseEntity.ok(CommonResponse.success("Text fields retrieved successfully", fields));
    }

    @GetMapping("/fields/date")
    @Operation(
        summary = "Get available date filter fields",
        description = "Returns list of available date fields for filtering"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getDateFields() {
        log.info("GET /strategies/dropdowns/fields/date - Fetching date fields");

        List<Map<String, String>> fields = Arrays.asList(
            Map.of("code", "DUE_DATE", "name", "Due Date", "description", "Payment due date"),
            Map.of("code", "DISB_DATE", "name", "Disbursement Date", "description", "Loan disbursement date"),
            Map.of("code", "EMI_START_DATE", "name", "EMI Start Date", "description", "EMI start date"),
            Map.of("code", "MATURITY_DATE", "name", "Maturity Date", "description", "Loan maturity date"),
            Map.of("code", "LAST_PAYMENT_DATE", "name", "Last Payment Date", "description", "Last payment date"),
            Map.of("code", "NEXT_EMI_DATE", "name", "Next EMI Date", "description", "Next EMI due date")
        );

        return ResponseEntity.ok(CommonResponse.success("Date fields retrieved successfully", fields));
    }

    // ==================== Days of Week ====================

    @GetMapping("/days")
    @Operation(
        summary = "Get days of week",
        description = "Returns list of days for weekly schedule configuration"
    )
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getDaysOfWeek() {
        log.info("GET /strategies/dropdowns/days - Fetching days of week");

        List<Map<String, String>> days = Arrays.asList(
            Map.of("code", "MONDAY", "name", "Monday", "shortName", "Mon"),
            Map.of("code", "TUESDAY", "name", "Tuesday", "shortName", "Tue"),
            Map.of("code", "WEDNESDAY", "name", "Wednesday", "shortName", "Wed"),
            Map.of("code", "THURSDAY", "name", "Thursday", "shortName", "Thu"),
            Map.of("code", "FRIDAY", "name", "Friday", "shortName", "Fri"),
            Map.of("code", "SATURDAY", "name", "Saturday", "shortName", "Sat"),
            Map.of("code", "SUNDAY", "name", "Sunday", "shortName", "Sun")
        );

        return ResponseEntity.ok(CommonResponse.success("Days of week retrieved successfully", days));
    }
}
