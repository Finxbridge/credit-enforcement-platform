package com.finx.management.controller;

import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.service.PermissionService;
import com.finx.common.domain.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/access/management/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<PermissionDTO>>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseWrapper.ok("Permissions retrieved successfully.", permissions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<PermissionDTO>> getPermissionById(@PathVariable Long id) {
        PermissionDTO permission = permissionService.getPermissionById(id);
        return ResponseWrapper.ok("Permission retrieved successfully.", permission);
    }
}
