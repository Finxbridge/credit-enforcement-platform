package com.finx.allocationreallocationservice.controller;

import com.finx.allocationreallocationservice.domain.dto.*;
import com.finx.allocationreallocationservice.service.ReallocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.RequestPart;

@Slf4j
@RestController
@RequestMapping("/reallocations")
@RequiredArgsConstructor
@Tag(name = "Reallocation Management", description = "APIs for case reallocation management")
public class ReallocationController {

    private final ReallocationService reallocationService;

    @PostMapping("/upload")
    @Operation(summary = "Upload CSV for bulk reallocation")
    public ResponseEntity<CommonResponse<AllocationBatchUploadResponseDTO>> uploadReallocationBatch(
            @RequestPart("file") MultipartFile file) {
        log.info("Received reallocation batch upload request");

        AllocationBatchUploadResponseDTO response = reallocationService.uploadReallocationBatch(file);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Bulk reallocation initiated.", response));
    }

    @PostMapping("/by-agent")
    @Operation(summary = "Reallocate all cases from one agent to another")
    public ResponseEntity<CommonResponse<ReallocationResponseDTO>> reallocateByAgent(
            @Valid @RequestBody ReallocationByAgentRequestDTO request) {
        log.info("Received reallocation by agent request from {} to {}",
                request.getFromUserId(), request.getToUserId());

        ReallocationResponseDTO response = reallocationService.reallocateByAgent(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Reallocation by agent initiated.", response));
    }

    @PostMapping("/by-filter")
    @Operation(summary = "Reallocate cases using filter criteria")
    public ResponseEntity<CommonResponse<ReallocationResponseDTO>> reallocateByFilter(
            @Valid @RequestBody ReallocationByFilterRequestDTO request) {
        log.info("Received reallocation by filter request to user {}", request.getToUserId());

        ReallocationResponseDTO response = reallocationService.reallocateByFilter(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.success("Reallocation by filter initiated.", response));
    }

    @GetMapping("/{batchId}/status")
    @Operation(summary = "Get reallocation batch status")
    public ResponseEntity<CommonResponse<AllocationBatchStatusDTO>> getReallocationBatchStatus(
            @PathVariable String batchId) {
        log.info("Fetching reallocation batch status for: {}", batchId);

        AllocationBatchStatusDTO status = reallocationService.getReallocationBatchStatus(batchId);

        return ResponseEntity.ok(CommonResponse.success(
                "Reallocation batch status retrieved successfully.", status));
    }

    @GetMapping("/{batchId}/errors")
    @Operation(summary = "Export failed reallocation rows")
    public ResponseEntity<byte[]> exportFailedReallocationRows(@PathVariable String batchId) {
        log.info("Exporting failed reallocation rows for batch: {}", batchId);

        byte[] csvData = reallocationService.exportFailedReallocationRows(batchId);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "failed_reallocations_" + batchId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
