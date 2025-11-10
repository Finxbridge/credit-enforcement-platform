package com.finx.management.controller;

import com.finx.management.domain.dto.CreateRoleRequest;
import com.finx.management.domain.dto.RoleDTO;
import com.finx.management.domain.dto.UpdateRoleRequest;
import com.finx.management.service.RoleService;
import com.finx.common.domain.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/access/management/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<CommonResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseWrapper.ok("Roles retrieved successfully.", roles);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/management")
    public ResponseEntity<CommonResponse<List<RoleDTO>>> getAllRolesForManagement() {
        List<RoleDTO> roles = roleService.getAllRoles(false);
        return ResponseWrapper.ok("All roles retrieved successfully.", roles);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<RoleDTO>> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseWrapper.ok("Role retrieved successfully.", role);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<CommonResponse<RoleDTO>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleDTO createdRole = roleService.createRole(request);
        return ResponseWrapper.created("Role created successfully.", createdRole);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<RoleDTO>> updateRole(@PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleDTO updatedRole = roleService.updateRole(id, request);
        return ResponseWrapper.ok("Role updated successfully.", updatedRole);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseWrapper.okMessage("Role deleted successfully.");
    }
}
