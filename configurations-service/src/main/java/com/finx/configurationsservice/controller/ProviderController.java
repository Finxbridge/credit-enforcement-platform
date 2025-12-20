package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.enums.ProviderType;
import com.finx.configurationsservice.service.ProviderService;
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
@RequestMapping("/api/v1/configurations/providers")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<CommonResponse<ProviderDTO>> createProvider(
            @Valid @RequestBody CreateProviderRequest request) {
        log.info("Creating provider: {}", request.getProviderCode());
        ProviderDTO provider = providerService.createProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(provider, "Provider created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ProviderDTO>> getProviderById(@PathVariable Long id) {
        ProviderDTO provider = providerService.getProviderById(id);
        return ResponseEntity.ok(ResponseWrapper.success(provider));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CommonResponse<ProviderDTO>> getProviderByCode(@PathVariable String code) {
        ProviderDTO provider = providerService.getProviderByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(provider));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<List<ProviderDTO>>> getProvidersByType(
            @PathVariable ProviderType type) {
        List<ProviderDTO> providers = providerService.getProvidersByType(type);
        return ResponseEntity.ok(ResponseWrapper.success(providers));
    }

    @GetMapping("/type/{type}/active")
    public ResponseEntity<CommonResponse<List<ProviderDTO>>> getActiveProvidersByType(
            @PathVariable ProviderType type) {
        List<ProviderDTO> providers = providerService.getActiveProvidersByType(type);
        return ResponseEntity.ok(ResponseWrapper.success(providers));
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<ProviderDTO>>> getActiveProviders() {
        List<ProviderDTO> providers = providerService.getActiveProviders();
        return ResponseEntity.ok(ResponseWrapper.success(providers));
    }

    @GetMapping("/type/{type}/default")
    public ResponseEntity<CommonResponse<ProviderDTO>> getDefaultProvider(
            @PathVariable ProviderType type) {
        ProviderDTO provider = providerService.getDefaultProvider(type);
        return ResponseEntity.ok(ResponseWrapper.success(provider));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ProviderDTO>> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProviderRequest request) {
        log.info("Updating provider: {}", id);
        ProviderDTO provider = providerService.updateProvider(id, request);
        return ResponseEntity.ok(ResponseWrapper.success(provider, "Provider updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deactivateProvider(@PathVariable Long id) {
        log.info("Deactivating provider: {}", id);
        providerService.deactivateProvider(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Provider deactivated successfully"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<Void>> activateProvider(@PathVariable Long id) {
        log.info("Activating provider: {}", id);
        providerService.activateProvider(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Provider activated successfully"));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<CommonResponse<ProviderTestResult>> testProvider(@PathVariable Long id) {
        log.info("Testing provider connectivity: {}", id);
        ProviderTestResult result = providerService.testProvider(id);
        return ResponseEntity.ok(ResponseWrapper.success(result, "Provider test completed"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProviderDTO>>> getAllProviders(
            @RequestParam(required = false) ProviderType type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProviderDTO> providers = providerService.getAllProviders(type, isActive, search, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(providers));
    }

    @GetMapping("/{id}/test-history")
    public ResponseEntity<CommonResponse<List<ProviderTestResult>>> getTestHistory(
            @PathVariable Long id) {
        List<ProviderTestResult> history = providerService.getTestHistory(id);
        return ResponseEntity.ok(ResponseWrapper.success(history));
    }
}
