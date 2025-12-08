package com.finx.management.controller;

import com.finx.management.domain.dto.CreateUserRequest;
import com.finx.management.domain.dto.UpdateUserRequest;
import com.finx.management.domain.dto.UserDTO;
import com.finx.management.domain.dto.UserListDTO;
import com.finx.management.domain.dto.UserPermissionDTO;
import com.finx.management.service.UserService;
import com.finx.common.domain.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/access/management/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @SuppressWarnings("null")
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<UserListDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<UserListDTO> users = userService.getAllUsers(pageable, search, status);
        return ResponseWrapper.ok("Users retrieved successfully.", users);
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseWrapper.ok("User retrieved successfully.", user);
    }

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping
    public ResponseEntity<CommonResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO createdUser = userService.createUser(request);
        return ResponseWrapper.created("User created successfully.", createdUser);
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<UserDTO>> updateUser(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseWrapper.ok("User updated successfully.", updatedUser);
    }

    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseWrapper.okMessage("User deleted successfully.");
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/{userId}/permissions")
    public ResponseEntity<CommonResponse<List<UserPermissionDTO>>> getUserPermissions(@PathVariable Long userId) {
        List<UserPermissionDTO> permissions = userService.getUserPermissions(userId);
        return ResponseWrapper.ok("User permissions retrieved successfully.", permissions);
    }
}
