package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.enums.LmsType;
import com.finx.configurationsservice.service.LmsConfigurationService;
import com.finx.configurationsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configurations/lms")
@RequiredArgsConstructor
@Slf4j
public class LmsConfigurationController {

    private final LmsConfigurationService lmsConfigService;

    @PostMapping
    public ResponseEntity<CommonResponse<LmsConfigurationDTO>> createLmsConfig(
            @Valid @RequestBody CreateLmsConfigRequest request) {
        log.info("Creating LMS configuration: {}", request.getLmsCode());
        LmsConfigurationDTO lmsConfig = lmsConfigService.createLmsConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(lmsConfig, "LMS configuration created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<LmsConfigurationDTO>> getLmsConfigById(@PathVariable Long id) {
        LmsConfigurationDTO lmsConfig = lmsConfigService.getLmsConfigById(id);
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfig));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CommonResponse<LmsConfigurationDTO>> getLmsConfigByCode(@PathVariable String code) {
        LmsConfigurationDTO lmsConfig = lmsConfigService.getLmsConfigByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfig));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<List<LmsConfigurationDTO>>> getLmsConfigsByType(
            @PathVariable LmsType type) {
        List<LmsConfigurationDTO> lmsConfigs = lmsConfigService.getLmsConfigsByType(type);
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfigs));
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<LmsConfigurationDTO>>> getActiveLmsConfigs() {
        List<LmsConfigurationDTO> lmsConfigs = lmsConfigService.getActiveLmsConfigs();
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfigs));
    }

    @GetMapping("/type/{type}/active")
    public ResponseEntity<CommonResponse<List<LmsConfigurationDTO>>> getActiveLmsConfigsByType(
            @PathVariable LmsType type) {
        List<LmsConfigurationDTO> lmsConfigs = lmsConfigService.getActiveLmsConfigsByType(type);
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfigs));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<LmsConfigurationDTO>> updateLmsConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLmsConfigRequest request) {
        log.info("Updating LMS configuration: {}", id);
        LmsConfigurationDTO lmsConfig = lmsConfigService.updateLmsConfig(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfig, "LMS configuration updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deactivateLmsConfig(@PathVariable Long id) {
        log.info("Deactivating LMS configuration: {}", id);
        lmsConfigService.deactivateLmsConfig(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "LMS configuration deactivated successfully"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<Void>> activateLmsConfig(@PathVariable Long id) {
        log.info("Activating LMS configuration: {}", id);
        lmsConfigService.activateLmsConfig(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "LMS configuration activated successfully"));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<CommonResponse<LmsConnectionTestResult>> testLmsConnection(@PathVariable Long id) {
        log.info("Testing LMS connection: {}", id);
        LmsConnectionTestResult result = lmsConfigService.testLmsConnection(id);
        return ResponseEntity.ok(ResponseWrapper.success(result, "LMS connection test completed"));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<CommonResponse<LmsSyncHistoryDTO>> triggerManualSync(@PathVariable Long id) {
        log.info("Triggering manual sync for LMS: {}", id);
        LmsSyncHistoryDTO syncHistory = lmsConfigService.triggerManualSync(id);
        return ResponseEntity.ok(ResponseWrapper.success(syncHistory, "Manual sync triggered successfully"));
    }

    @GetMapping("/{id}/sync-history")
    public ResponseEntity<CommonResponse<Page<LmsSyncHistoryDTO>>> getSyncHistory(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LmsSyncHistoryDTO> history = lmsConfigService.getSyncHistory(id, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(history));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<LmsConfigurationDTO>>> getAllLmsConfigs(
            @RequestParam(required = false) LmsType type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LmsConfigurationDTO> lmsConfigs = lmsConfigService.getAllLmsConfigs(type, isActive, search, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(lmsConfigs));
    }
}
