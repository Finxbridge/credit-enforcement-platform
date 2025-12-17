package com.finx.collectionsservice.controller;

import com.finx.collectionsservice.domain.dto.*;
import com.finx.collectionsservice.service.CycleClosureService;
import com.finx.collectionsservice.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collections/closure")
@RequiredArgsConstructor
@Tag(name = "Cycle Closure", description = "APIs for case closure management")
public class CycleClosureController {

    private final CycleClosureService closureService;

    @PostMapping
    @Operation(summary = "Close a case", description = "Close a case with closure reason")
    public ResponseEntity<CommonResponse<CaseClosureResponse>> closeCase(
            @Valid @RequestBody CaseClosureRequest request) {
        log.info("POST /closure - Closing case {} with reason: {}", request.getCaseId(), request.getClosureReason());
        CaseClosureResponse response = closureService.closeCase(request.getCaseId(), request.getClosureReason());
        return ResponseWrapper.ok(response.getMessage(), response);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Close multiple cases", description = "Close multiple cases in bulk")
    public ResponseEntity<CommonResponse<CaseClosureResponse>> closeCasesBulk(
            @RequestParam List<Long> caseIds,
            @RequestParam String closureReason) {
        log.info("POST /closure/bulk - Closing {} cases", caseIds.size());
        CaseClosureResponse response = closureService.closeCasesBulk(caseIds, closureReason);
        return ResponseWrapper.ok(response.getMessage(), response);
    }

    @PostMapping("/{caseId}/reopen")
    @Operation(summary = "Reopen a closed case", description = "Reopen a previously closed case")
    public ResponseEntity<CommonResponse<CaseClosureResponse>> reopenCase(@PathVariable Long caseId) {
        log.info("POST /closure/{}/reopen - Reopening case", caseId);
        CaseClosureResponse response = closureService.reopenCase(caseId);
        return ResponseWrapper.ok(response.getMessage(), response);
    }

    @GetMapping
    @Operation(summary = "Get closed cases", description = "Get all closed cases with pagination")
    public ResponseEntity<CommonResponse<Page<CaseClosureResponse>>> getClosedCases(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /closure - Fetching closed cases");
        Page<CaseClosureResponse> cases = closureService.getClosedCases(pageable);
        return ResponseWrapper.ok("Closed cases retrieved successfully", cases);
    }

    @GetMapping("/case/{caseId}/history")
    @Operation(summary = "Get case closure history", description = "Get closure history for a specific case")
    public ResponseEntity<CommonResponse<List<CaseClosureResponse>>> getCaseClosureHistory(
            @PathVariable Long caseId) {
        log.info("GET /closure/case/{}/history - Fetching closure history", caseId);
        List<CaseClosureResponse> history = closureService.getCaseClosureHistory(caseId);
        return ResponseWrapper.ok("Closure history retrieved successfully", history);
    }
}
