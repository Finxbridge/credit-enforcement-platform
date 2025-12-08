package com.finx.strategyengineservice.service.impl;

import com.finx.strategyengineservice.domain.dto.FilterMetadataResponseV2;
import com.finx.strategyengineservice.domain.entity.FilterField;
import com.finx.strategyengineservice.domain.entity.FilterFieldOption;
import com.finx.strategyengineservice.repository.FilterFieldRepository;
import com.finx.strategyengineservice.service.FilterFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterFieldServiceImpl implements FilterFieldService {

    private final FilterFieldRepository filterFieldRepository;

    @Override
    @Transactional(readOnly = true)
    public FilterMetadataResponseV2 getAllFilterMetadata() {
        log.info("Fetching all active filter metadata from database");

        List<FilterField> textFilters = filterFieldRepository.findByFieldTypeAndIsActiveTrueOrderBySortOrder("TEXT");
        List<FilterField> dateFilters = filterFieldRepository.findByFieldTypeAndIsActiveTrueOrderBySortOrder("DATE");
        List<FilterField> numericFilters = filterFieldRepository.findByFieldTypeAndIsActiveTrueOrderBySortOrder("NUMERIC");

        FilterMetadataResponseV2.FilterData data = FilterMetadataResponseV2.FilterData.builder()
                .attributes(buildAttributes(textFilters))
                .datefields(buildFieldInfo(dateFilters))
                .numberfields(buildFieldInfo(numericFilters))
                .numericOperators(buildNumericOperators())
                .dateOperators(buildDateOperators())
                .build();

        return FilterMetadataResponseV2.builder()
                .code("S004")
                .status("200")
                .msg("Displaying top results")
                .data(data)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilterField> getAllActiveFilters() {
        return filterFieldRepository.findByIsActiveTrueOrderBySortOrder();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilterField> getActiveFiltersByType(String fieldType) {
        return filterFieldRepository.findByFieldTypeAndIsActiveTrueOrderBySortOrder(fieldType.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public FilterField getFilterByCode(String fieldCode) {
        return filterFieldRepository.findByFieldCode(fieldCode)
                .orElseThrow(() -> new RuntimeException("Filter field not found: " + fieldCode));
    }

    @Override
    @Transactional
    public FilterField createFilter(FilterField filterField) {
        if (filterFieldRepository.existsByFieldCode(filterField.getFieldCode())) {
            throw new RuntimeException("Filter field with code " + filterField.getFieldCode() + " already exists");
        }
        return filterFieldRepository.save(filterField);
    }

    @Override
    @Transactional
    public FilterField updateFilter(Long id, FilterField filterField) {
        FilterField existing = filterFieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filter field not found: " + id));

        existing.setDisplayName(filterField.getDisplayName());
        existing.setDescription(filterField.getDescription());
        existing.setIsActive(filterField.getIsActive());
        existing.setSortOrder(filterField.getSortOrder());

        return filterFieldRepository.save(existing);
    }

    @Override
    @Transactional
    public void toggleFilterStatus(Long id, Boolean isActive) {
        FilterField filterField = filterFieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filter field not found: " + id));

        filterField.setIsActive(isActive);
        filterFieldRepository.save(filterField);

        log.info("Filter field {} status changed to: {}", filterField.getFieldCode(), isActive);
    }

    @Override
    @Transactional
    public void deleteFilter(Long id) {
        filterFieldRepository.deleteById(id);
        log.info("Filter field deleted: {}", id);
    }

    // Helper methods to build response structure
    private List<FilterMetadataResponseV2.Attribute> buildAttributes(List<FilterField> textFilters) {
        return textFilters.stream()
                .map(filter -> FilterMetadataResponseV2.Attribute.builder()
                        .key(filter.getFieldKey())
                        .type(filter.getFieldType())
                        .options(filter.getOptions().stream()
                                .filter(FilterFieldOption::getIsActive)
                                .map(opt -> FilterMetadataResponseV2.Option.builder()
                                        ._id(opt.getId().toString())
                                        .name(opt.getOptionValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<FilterMetadataResponseV2.FieldInfo> buildFieldInfo(List<FilterField> filters) {
        return filters.stream()
                .map(filter -> FilterMetadataResponseV2.FieldInfo.builder()
                        .key(filter.getFieldKey())
                        .label(filter.getDisplayName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<FilterMetadataResponseV2.OperatorInfo> buildNumericOperators() {
        return Arrays.asList(
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("GREATER_THAN_EQUAL")
                        .symbol(">=")
                        .displayName("Greater Than or Equal")
                        .description("Value should be Minimum Value")
                        .build(),
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("LESS_THAN_EQUAL")
                        .symbol("<=")
                        .displayName("Less Than or Equal")
                        .description("Value should be Maximum Value")
                        .build(),
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("EQUAL")
                        .symbol("=")
                        .displayName("Equal")
                        .description("Add exact value")
                        .build(),
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("RANGE")
                        .symbol("RANGE")
                        .displayName("Range")
                        .description("Values should be Minimum and Maximum")
                        .build()
        );
    }

    private List<FilterMetadataResponseV2.OperatorInfo> buildDateOperators() {
        return Arrays.asList(
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("OLDER_THAN")
                        .symbol("<")
                        .displayName("Older Than")
                        .description("Date is older than specified date")
                        .build(),
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("NEWER_THAN")
                        .symbol(">")
                        .displayName("Newer Than")
                        .description("Date is newer than specified date")
                        .build(),
                FilterMetadataResponseV2.OperatorInfo.builder()
                        .code("INTERVAL")
                        .symbol("BETWEEN")
                        .displayName("Interval")
                        .description("Date is between two dates")
                        .build()
        );
    }
}
