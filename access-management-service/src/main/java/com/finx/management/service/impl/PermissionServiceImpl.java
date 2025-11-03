package com.finx.management.service.impl;

import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.exception.ResourceNotFoundException;
import com.finx.management.mapper.PermissionMapper;
import com.finx.management.repository.ManagementPermissionRepository;
import com.finx.management.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final ManagementPermissionRepository managementPermissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return managementPermissionRepository.findAll().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        return managementPermissionRepository.findById(id)
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }
}
