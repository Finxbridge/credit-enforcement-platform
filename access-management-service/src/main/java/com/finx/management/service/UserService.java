package com.finx.management.service;

import com.finx.management.domain.dto.CreateUserRequest;
import com.finx.management.domain.dto.UpdateUserRequest;
import com.finx.management.domain.dto.UserDTO;
import com.finx.management.domain.dto.UserPermissionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<UserDTO> getAllUsers(Pageable pageable, String search, String status);

    UserDTO getUserById(Long id);

    UserDTO createUser(CreateUserRequest request);

    UserDTO updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    List<UserPermissionDTO> getUserPermissions(Long userId);
}
