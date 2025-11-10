package com.finx.management.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.constants.CacheConstants;
import com.finx.management.domain.dto.PermissionDTO;
import com.finx.management.exception.ResourceNotFoundException;
import com.finx.management.mapper.PermissionMapper;
import com.finx.management.repository.ManagementPermissionRepository;
import com.finx.management.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final ManagementPermissionRepository managementPermissionRepository;
    private final PermissionMapper permissionMapper;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;


    @Override
    public List<PermissionDTO> getAllPermissions() {
        return managementPermissionRepository.findAll().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Cache cache = cacheManager.getCache(CacheConstants.PERMISSIONS);
        if (Objects.nonNull(cache)) {
            Object cachedPermission = cache.get(id, Object.class);
            if (cachedPermission instanceof LinkedHashMap) {
                return objectMapper.convertValue(cachedPermission, PermissionDTO.class);
            } else if (cachedPermission instanceof PermissionDTO) {
                return (PermissionDTO) cachedPermission;
            }
        }

        PermissionDTO permissionDTO = managementPermissionRepository.findById(id)
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        if(Objects.nonNull(cache)){
            cache.put(id, permissionDTO);
        }

        return permissionDTO;
    }
}
