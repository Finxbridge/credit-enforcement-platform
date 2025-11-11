package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.dto.*;

import java.util.List;

public interface StrategyService {

    List<StrategyDTO> getAllStrategies();

    StrategyDTO createStrategy(CreateStrategyRequest request);

    StrategyDetailDTO getStrategyById(Long strategyId);

    StrategyDTO updateStrategy(Long strategyId, UpdateStrategyRequest request);

    void deleteStrategy(Long strategyId);

    FiltersResponse updateFilters(Long strategyId, FiltersRequest request);

    FiltersResponse getFilters(Long strategyId);

    TemplateInfoResponse updateTemplate(Long strategyId, TemplateInfoRequest request);

    TemplateInfoResponse getTemplate(Long strategyId);

    TriggerConfigResponse configureTrigger(Long strategyId, TriggerConfigRequest request);

    TriggerConfigResponse updateTrigger(Long strategyId, TriggerConfigRequest request);

    SimulationResultDTO simulateStrategy(Long strategyId);
}
