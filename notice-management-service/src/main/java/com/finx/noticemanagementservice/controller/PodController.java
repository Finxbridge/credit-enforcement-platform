package com.finx.noticemanagementservice.controller;

import com.finx.noticemanagementservice.domain.dto.CommonResponse;
import com.finx.noticemanagementservice.domain.dto.ProofOfDeliveryDTO;
import com.finx.noticemanagementservice.domain.dto.UploadPodRequest;
import com.finx.noticemanagementservice.domain.dto.VerifyPodRequest;
import com.finx.noticemanagementservice.domain.enums.PodVerificationStatus;
import com.finx.noticemanagementservice.service.PodService;
import com.finx.noticemanagementservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notices/pods")
@RequiredArgsConstructor
public class PodController {

    private final PodService podService;

    @PostMapping
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> uploadPod(
            @Valid @RequestBody UploadPodRequest request) {
        ProofOfDeliveryDTO pod = podService.uploadPod(request);
        return ResponseWrapper.created("POD uploaded successfully", pod);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> getPodById(@PathVariable Long id) {
        ProofOfDeliveryDTO pod = podService.getPodById(id);
        return ResponseWrapper.ok("POD retrieved successfully", pod);
    }

    @GetMapping("/number/{podNumber}")
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> getPodByNumber(@PathVariable String podNumber) {
        ProofOfDeliveryDTO pod = podService.getPodByNumber(podNumber);
        return ResponseWrapper.ok("POD retrieved successfully", pod);
    }

    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> getPodByNoticeId(@PathVariable Long noticeId) {
        ProofOfDeliveryDTO pod = podService.getPodByNoticeId(noticeId);
        return ResponseWrapper.ok("POD retrieved successfully", pod);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<CommonResponse<List<ProofOfDeliveryDTO>>> getPodsByVendor(@PathVariable Long vendorId) {
        List<ProofOfDeliveryDTO> pods = podService.getPodsByVendor(vendorId);
        return ResponseWrapper.ok("PODs retrieved successfully", pods);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<CommonResponse<Page<ProofOfDeliveryDTO>>> getPodsByVerificationStatus(
            @PathVariable PodVerificationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProofOfDeliveryDTO> pods = podService.getPodsByVerificationStatus(status, pageable);
        return ResponseWrapper.ok("PODs retrieved successfully", pods);
    }

    @GetMapping("/upload-date-range")
    public ResponseEntity<CommonResponse<Page<ProofOfDeliveryDTO>>> getPodsByUploadDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProofOfDeliveryDTO> pods = podService.getPodsByUploadDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("PODs retrieved successfully", pods);
    }

    @GetMapping("/delivery-date-range")
    public ResponseEntity<CommonResponse<Page<ProofOfDeliveryDTO>>> getPodsByDeliveryDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProofOfDeliveryDTO> pods = podService.getPodsByDeliveryDateRange(startDate, endDate, pageable);
        return ResponseWrapper.ok("PODs retrieved successfully", pods);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProofOfDeliveryDTO>>> getAllPods(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProofOfDeliveryDTO> pods = podService.getAllPods(pageable);
        return ResponseWrapper.ok("PODs retrieved successfully", pods);
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<CommonResponse<List<ProofOfDeliveryDTO>>> getPendingVerifications() {
        List<ProofOfDeliveryDTO> pods = podService.getPendingVerifications();
        return ResponseWrapper.ok("Pending verifications retrieved successfully", pods);
    }

    @PostMapping("/verify")
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> verifyPod(
            @Valid @RequestBody VerifyPodRequest request) {
        ProofOfDeliveryDTO pod = podService.verifyPod(request);
        return ResponseWrapper.ok("POD verified successfully", pod);
    }

    @PutMapping("/{podId}/approve")
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> approvePod(
            @PathVariable Long podId,
            @RequestParam(required = false) String remarks,
            @RequestParam Long verifiedBy) {
        ProofOfDeliveryDTO pod = podService.approvePod(podId, remarks, verifiedBy);
        return ResponseWrapper.ok("POD approved successfully", pod);
    }

    @PutMapping("/{podId}/reject")
    public ResponseEntity<CommonResponse<ProofOfDeliveryDTO>> rejectPod(
            @PathVariable Long podId,
            @RequestParam String rejectionReason,
            @RequestParam Long verifiedBy) {
        ProofOfDeliveryDTO pod = podService.rejectPod(podId, rejectionReason, verifiedBy);
        return ResponseWrapper.ok("POD rejected successfully", pod);
    }

    @GetMapping("/count/{status}")
    public ResponseEntity<CommonResponse<Long>> countByVerificationStatus(
            @PathVariable PodVerificationStatus status) {
        Long count = podService.countByVerificationStatus(status);
        return ResponseWrapper.ok("Count retrieved successfully", count);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deletePod(@PathVariable Long id) {
        podService.deletePod(id);
        return ResponseWrapper.okMessage("POD deleted successfully");
    }
}
