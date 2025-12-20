package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreateOrganizationRequest;
import com.finx.configurationsservice.domain.dto.OrganizationDTO;
import com.finx.configurationsservice.service.OrganizationService;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/config/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<CommonResponse<OrganizationDTO>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        log.info("POST /organizations - Creating organization: {}", request.getOrgCode());
        OrganizationDTO response = organizationService.createOrganization(request);
        return ResponseWrapper.created("Organization created successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<OrganizationDTO>> getOrganizationById(@PathVariable Long id) {
        log.info("GET /organizations/{} - Fetching organization", id);
        OrganizationDTO response = organizationService.getOrganizationById(id);
        return ResponseWrapper.ok("Organization retrieved successfully", response);
    }

    @GetMapping("/code/{orgCode}")
    public ResponseEntity<CommonResponse<OrganizationDTO>> getOrganizationByCode(@PathVariable String orgCode) {
        log.info("GET /organizations/code/{} - Fetching organization", orgCode);
        OrganizationDTO response = organizationService.getOrganizationByCode(orgCode);
        return ResponseWrapper.ok("Organization retrieved successfully", response);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<OrganizationDTO>>> getActiveOrganizations() {
        log.info("GET /organizations/active - Fetching active organizations");
        List<OrganizationDTO> organizations = organizationService.getActiveOrganizations();
        return ResponseWrapper.ok("Active organizations retrieved successfully", organizations);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<OrganizationDTO>>> getAllOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /organizations - Fetching all organizations");
        Page<OrganizationDTO> organizations = organizationService.getAllOrganizations(pageable);
        return ResponseWrapper.ok("Organizations retrieved successfully", organizations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<OrganizationDTO>> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody CreateOrganizationRequest request) {
        log.info("PUT /organizations/{} - Updating organization", id);
        OrganizationDTO response = organizationService.updateOrganization(id, request);
        return ResponseWrapper.ok("Organization updated successfully", response);
    }

    @PutMapping("/{id}/features")
    public ResponseEntity<CommonResponse<OrganizationDTO>> updateFeatures(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> features) {
        log.info("PUT /organizations/{}/features - Updating features", id);
        OrganizationDTO response = organizationService.updateFeatures(id, features);
        return ResponseWrapper.ok("Organization features updated successfully", response);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<OrganizationDTO>> activateOrganization(@PathVariable Long id) {
        log.info("POST /organizations/{}/activate - Activating organization", id);
        OrganizationDTO response = organizationService.activateOrganization(id);
        return ResponseWrapper.ok("Organization activated successfully", response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<OrganizationDTO>> deactivateOrganization(@PathVariable Long id) {
        log.info("POST /organizations/{}/deactivate - Deactivating organization", id);
        OrganizationDTO response = organizationService.deactivateOrganization(id);
        return ResponseWrapper.ok("Organization deactivated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteOrganization(@PathVariable Long id) {
        log.info("DELETE /organizations/{} - Deleting organization", id);
        organizationService.deleteOrganization(id);
        return ResponseWrapper.ok("Organization deleted successfully", null);
    }

    @GetMapping("/{id}/license-valid")
    public ResponseEntity<CommonResponse<Boolean>> isLicenseValid(@PathVariable Long id) {
        log.info("GET /organizations/{}/license-valid - Checking license validity", id);
        boolean isValid = organizationService.isLicenseValid(id);
        return ResponseWrapper.ok("License validity checked", isValid);
    }

    @GetMapping("/{id}/feature-enabled/{featureName}")
    public ResponseEntity<CommonResponse<Boolean>> isFeatureEnabled(
            @PathVariable Long id,
            @PathVariable String featureName) {
        log.info("GET /organizations/{}/feature-enabled/{} - Checking feature", id, featureName);
        boolean isEnabled = organizationService.isFeatureEnabled(id, featureName);
        return ResponseWrapper.ok("Feature status checked", isEnabled);
    }
}
