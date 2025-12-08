package com.finx.strategyengineservice.service;

import com.finx.strategyengineservice.domain.dto.FilterMetadataResponseV2;
import com.finx.strategyengineservice.domain.entity.FilterField;

import java.util.List;

public interface FilterFieldService {

    FilterMetadataResponseV2 getAllFilterMetadata();

    List<FilterField> getAllActiveFilters();

    List<FilterField> getActiveFiltersByType(String fieldType);

    FilterField getFilterByCode(String fieldCode);

    FilterField createFilter(FilterField filterField);

    FilterField updateFilter(Long id, FilterField filterField);

    void toggleFilterStatus(Long id, Boolean isActive);

    void deleteFilter(Long id);
}
