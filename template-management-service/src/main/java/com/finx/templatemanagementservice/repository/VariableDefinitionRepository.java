package com.finx.templatemanagementservice.repository;

import com.finx.templatemanagementservice.domain.entity.VariableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableDefinitionRepository extends JpaRepository<VariableDefinition, Long> {

    Optional<VariableDefinition> findByVariableKey(String variableKey);

    List<VariableDefinition> findByIsActiveTrue();

    List<VariableDefinition> findByCategory(String category);

    List<VariableDefinition> findByCategoryAndIsActiveTrue(String category);

    boolean existsByVariableKey(String variableKey);

    List<VariableDefinition> findByVariableKeyIn(List<String> variableKeys);
}
