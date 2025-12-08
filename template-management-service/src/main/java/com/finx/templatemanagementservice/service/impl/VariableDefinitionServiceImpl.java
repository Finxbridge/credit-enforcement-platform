package com.finx.templatemanagementservice.service.impl;

import com.finx.templatemanagementservice.domain.dto.CreateVariableDefinitionRequest;
import com.finx.templatemanagementservice.domain.dto.VariableDefinitionDTO;
import com.finx.templatemanagementservice.domain.entity.VariableDefinition;
import com.finx.templatemanagementservice.exception.ResourceNotFoundException;
import com.finx.templatemanagementservice.exception.TemplateAlreadyExistsException;
import com.finx.templatemanagementservice.repository.VariableDefinitionRepository;
import com.finx.templatemanagementservice.service.VariableDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VariableDefinitionServiceImpl implements VariableDefinitionService {

    private final VariableDefinitionRepository variableDefinitionRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"variables", "variablesByCategory"}, allEntries = true)
    public VariableDefinitionDTO createVariable(CreateVariableDefinitionRequest request) {
        log.info("Creating variable definition: {}", request.getVariableKey());

        // Check if variable key already exists
        if (variableDefinitionRepository.existsByVariableKey(request.getVariableKey())) {
            throw new TemplateAlreadyExistsException("Variable with key '" + request.getVariableKey() + "' already exists");
        }

        VariableDefinition variable = VariableDefinition.builder()
                .variableKey(request.getVariableKey())
                .displayName(request.getDisplayName())
                .entityPath(request.getEntityPath())
                .dataType(request.getDataType())
                .defaultValue(request.getDefaultValue())
                .transformer(request.getTransformer())
                .description(request.getDescription())
                .category(request.getCategory())
                .exampleValue(request.getExampleValue())
                .isActive(true)
                .build();

        variable = variableDefinitionRepository.save(variable);
        log.info("Variable definition created successfully with id: {}", variable.getId());

        return mapToDTO(variable);
    }

    @Override
    @Cacheable(value = "variables", key = "#id")
    public VariableDefinitionDTO getVariable(Long id) {
        log.info("Fetching variable definition with id: {}", id);
        VariableDefinition variable = variableDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable not found with id: " + id));
        return mapToDTO(variable);
    }

    @Override
    @Cacheable(value = "variables", key = "#variableKey")
    public VariableDefinitionDTO getVariableByKey(String variableKey) {
        log.info("Fetching variable definition with key: {}", variableKey);
        VariableDefinition variable = variableDefinitionRepository.findByVariableKey(variableKey)
                .orElseThrow(() -> new ResourceNotFoundException("Variable not found with key: " + variableKey));
        return mapToDTO(variable);
    }

    @Override
    public List<VariableDefinitionDTO> getAllVariables() {
        log.info("Fetching all variable definitions");
        return variableDefinitionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "variables", key = "'active'")
    public List<VariableDefinitionDTO> getActiveVariables() {
        log.info("Fetching active variable definitions");
        return variableDefinitionRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "variablesByCategory", key = "#category")
    public List<VariableDefinitionDTO> getVariablesByCategory(String category) {
        log.info("Fetching variable definitions for category: {}", category);
        return variableDefinitionRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"variables", "variablesByCategory"}, allEntries = true)
    public VariableDefinitionDTO updateVariable(Long id, CreateVariableDefinitionRequest request) {
        log.info("Updating variable definition with id: {}", id);

        VariableDefinition variable = variableDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable not found with id: " + id));

        // Check if new variable key conflicts with existing (excluding current)
        if (!variable.getVariableKey().equals(request.getVariableKey()) &&
                variableDefinitionRepository.existsByVariableKey(request.getVariableKey())) {
            throw new TemplateAlreadyExistsException("Variable with key '" + request.getVariableKey() + "' already exists");
        }

        variable.setVariableKey(request.getVariableKey());
        variable.setDisplayName(request.getDisplayName());
        variable.setEntityPath(request.getEntityPath());
        variable.setDataType(request.getDataType());
        variable.setDefaultValue(request.getDefaultValue());
        variable.setTransformer(request.getTransformer());
        variable.setDescription(request.getDescription());
        variable.setCategory(request.getCategory());
        variable.setExampleValue(request.getExampleValue());

        variable = variableDefinitionRepository.save(variable);
        log.info("Variable definition updated successfully");

        return mapToDTO(variable);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"variables", "variablesByCategory"}, allEntries = true)
    public void deleteVariable(Long id) {
        log.info("Deleting variable definition with id: {}", id);

        if (!variableDefinitionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Variable not found with id: " + id);
        }

        variableDefinitionRepository.deleteById(id);
        log.info("Variable definition deleted successfully");
    }

    @Override
    @Transactional
    @CacheEvict(value = {"variables", "variablesByCategory"}, allEntries = true)
    public void toggleVariableStatus(Long id, Boolean isActive) {
        log.info("Toggling variable status for id: {} to {}", id, isActive);

        VariableDefinition variable = variableDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable not found with id: " + id));

        variable.setIsActive(isActive);
        variableDefinitionRepository.save(variable);

        log.info("Variable status updated successfully");
    }

    private VariableDefinitionDTO mapToDTO(VariableDefinition variable) {
        return VariableDefinitionDTO.builder()
                .id(variable.getId())
                .variableKey(variable.getVariableKey())
                .displayName(variable.getDisplayName())
                .entityPath(variable.getEntityPath())
                .dataType(variable.getDataType())
                .defaultValue(variable.getDefaultValue())
                .transformer(variable.getTransformer())
                .description(variable.getDescription())
                .category(variable.getCategory())
                .exampleValue(variable.getExampleValue())
                .isActive(variable.getIsActive())
                .createdAt(variable.getCreatedAt())
                .updatedAt(variable.getUpdatedAt())
                .build();
    }
}
