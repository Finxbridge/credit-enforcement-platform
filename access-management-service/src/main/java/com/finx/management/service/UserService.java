package com.finx.management.service;

import com.finx.management.domain.dto.AgencyDropdownDTO;
import com.finx.management.domain.dto.CreateUserRequest;
import com.finx.management.domain.dto.UpdateUserRequest;
import com.finx.management.domain.dto.UserDTO;
import com.finx.management.domain.dto.UserListDTO;
import com.finx.management.domain.dto.UserPermissionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<UserListDTO> getAllUsers(Pageable pageable, String search, String status);

    UserDTO getUserById(Long id);

    UserDTO createUser(CreateUserRequest request);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    List<UserPermissionDTO> getUserPermissions(Long userId);

    /**
     * Get list of approved (active) agencies for dropdown in user creation.
     * Used when creating a user with AGENT role.
     */
    List<AgencyDropdownDTO> getApprovedAgenciesForDropdown();
}
