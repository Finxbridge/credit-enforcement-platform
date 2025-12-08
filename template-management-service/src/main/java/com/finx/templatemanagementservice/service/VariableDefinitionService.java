package com.finx.templatemanagementservice.service;

import com.finx.templatemanagementservice.domain.dto.CreateVariableDefinitionRequest;
import com.finx.templatemanagementservice.domain.dto.VariableDefinitionDTO;

import java.util.List;

public interface VariableDefinitionService {

    VariableDefinitionDTO createVariable(CreateVariableDefinitionRequest request);

    VariableDefinitionDTO getVariable(Long id);

    VariableDefinitionDTO getVariableByKey(String variableKey);

    List<VariableDefinitionDTO> getAllVariables();

    List<VariableDefinitionDTO> getActiveVariables();

    List<VariableDefinitionDTO> getVariablesByCategory(String category);

    VariableDefinitionDTO updateVariable(Long id, CreateVariableDefinitionRequest request);

    void deleteVariable(Long id);

    void toggleVariableStatus(Long id, Boolean isActive);
}
