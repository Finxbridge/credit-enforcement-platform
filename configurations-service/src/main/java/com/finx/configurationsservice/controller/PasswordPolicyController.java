package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreatePasswordPolicyRequest;
import com.finx.configurationsservice.domain.dto.PasswordPolicyDTO;
import com.finx.configurationsservice.domain.enums.PolicyLevel;
import com.finx.configurationsservice.service.PasswordPolicyService;
import com.finx.configurationsservice.util.ResponseWrapper;
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
@RequestMapping("/config/password-policies")
@RequiredArgsConstructor
public class PasswordPolicyController {

    private final PasswordPolicyService policyService;

    @PostMapping
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> createPolicy(
            @Valid @RequestBody CreatePasswordPolicyRequest request) {
        log.info("POST /password-policies - Creating policy: {}", request.getPolicyCode());
        PasswordPolicyDTO response = policyService.createPolicy(request);
        return ResponseWrapper.created("Password policy created successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> getPolicyById(@PathVariable Long id) {
        log.info("GET /password-policies/{} - Fetching policy", id);
        PasswordPolicyDTO response = policyService.getPolicyById(id);
        return ResponseWrapper.ok("Password policy retrieved successfully", response);
    }

    @GetMapping("/code/{policyCode}")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> getPolicyByCode(@PathVariable String policyCode) {
        log.info("GET /password-policies/code/{} - Fetching policy", policyCode);
        PasswordPolicyDTO response = policyService.getPolicyByCode(policyCode);
        return ResponseWrapper.ok("Password policy retrieved successfully", response);
    }

    @GetMapping("/default")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> getDefaultPolicy() {
        log.info("GET /password-policies/default - Fetching default policy");
        PasswordPolicyDTO response = policyService.getDefaultPolicy();
        return ResponseWrapper.ok("Default password policy retrieved successfully", response);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<PasswordPolicyDTO>>> getActivePolicies() {
        log.info("GET /password-policies/active - Fetching active policies");
        List<PasswordPolicyDTO> policies = policyService.getActivePolicies();
        return ResponseWrapper.ok("Active password policies retrieved successfully", policies);
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<CommonResponse<List<PasswordPolicyDTO>>> getPoliciesByLevel(
            @PathVariable PolicyLevel level) {
        log.info("GET /password-policies/level/{} - Fetching policies", level);
        List<PasswordPolicyDTO> policies = policyService.getPoliciesByLevel(level);
        return ResponseWrapper.ok("Password policies retrieved successfully", policies);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<PasswordPolicyDTO>>> getAllPolicies(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /password-policies - Fetching all policies");
        Page<PasswordPolicyDTO> policies = policyService.getAllPolicies(pageable);
        return ResponseWrapper.ok("Password policies retrieved successfully", policies);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody CreatePasswordPolicyRequest request) {
        log.info("PUT /password-policies/{} - Updating policy", id);
        PasswordPolicyDTO response = policyService.updatePolicy(id, request);
        return ResponseWrapper.ok("Password policy updated successfully", response);
    }

    @PostMapping("/{id}/set-default")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> setAsDefault(@PathVariable Long id) {
        log.info("POST /password-policies/{}/set-default - Setting as default", id);
        PasswordPolicyDTO response = policyService.setAsDefault(id);
        return ResponseWrapper.ok("Default password policy set successfully", response);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> activatePolicy(@PathVariable Long id) {
        log.info("POST /password-policies/{}/activate - Activating policy", id);
        PasswordPolicyDTO response = policyService.activatePolicy(id);
        return ResponseWrapper.ok("Password policy activated successfully", response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<PasswordPolicyDTO>> deactivatePolicy(@PathVariable Long id) {
        log.info("POST /password-policies/{}/deactivate - Deactivating policy", id);
        PasswordPolicyDTO response = policyService.deactivatePolicy(id);
        return ResponseWrapper.ok("Password policy deactivated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deletePolicy(@PathVariable Long id) {
        log.info("DELETE /password-policies/{} - Deleting policy", id);
        policyService.deletePolicy(id);
        return ResponseWrapper.ok("Password policy deleted successfully", null);
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<CommonResponse<Boolean>> validatePassword(
            @PathVariable Long id,
            @RequestBody String password) {
        log.info("POST /password-policies/{}/validate - Validating password", id);
        boolean isValid = policyService.validatePassword(password, id);
        return ResponseWrapper.ok("Password validation completed", isValid);
    }
}
