package com.finx.management.controller;

import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.service.PermissionService;
import com.finx.common.domain.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/access/management/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "APIs for managing permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @GetMapping
    @Operation(summary = "Get all permissions")
    public ResponseEntity<CommonResponse<List<PermissionDTO>>> getAllPermissions() {
        log.info("Fetching all permissions");
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseWrapper.ok("Permissions retrieved successfully.", permissions);
    }

    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<CommonResponse<PermissionDTO>> getPermissionById(@PathVariable Long id) {
        log.info("Fetching permission with ID: {}", id);
        PermissionDTO permission = permissionService.getPermissionById(id);
        return ResponseWrapper.ok("Permission retrieved successfully.", permission);
    }

    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    @PostMapping
    @Operation(summary = "Create a new permission")
    public ResponseEntity<CommonResponse<PermissionDTO>> createPermission(
            @Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("Creating new permission with code: {}", permissionDTO.getCode());
        PermissionDTO createdPermission = permissionService.createPermission(permissionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Permission created successfully.", createdPermission));
    }

    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing permission")
    public ResponseEntity<CommonResponse<PermissionDTO>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("Updating permission with ID: {}", id);
        PermissionDTO updatedPermission = permissionService.updatePermission(id, permissionDTO);
        return ResponseWrapper.ok("Permission updated successfully.", updatedPermission);
    }

    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission")
    public ResponseEntity<CommonResponse<Void>> deletePermission(@PathVariable Long id) {
        log.info("Deleting permission with ID: {}", id);
        permissionService.deletePermission(id);
        return ResponseWrapper.ok("Permission deleted successfully.", null);
    }
}
