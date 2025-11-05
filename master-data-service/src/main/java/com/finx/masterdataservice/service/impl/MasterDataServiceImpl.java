package com.finx.masterdataservice.service.impl;

import com.finx.masterdataservice.constants.CacheConstants;
import com.finx.masterdataservice.exception.BusinessException;
import com.finx.masterdataservice.exception.ValidationException;
import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import com.finx.masterdataservice.domain.entity.MasterData;
import com.finx.masterdataservice.mapper.MasterDataMapper;
import com.finx.masterdataservice.repository.MasterDataRepository;
import com.finx.masterdataservice.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataServiceImpl implements MasterDataService {

    private final MasterDataRepository masterDataRepository;
    private final MasterDataMapper masterDataMapper;

    @Override
    @Cacheable(value = CacheConstants.MASTER_DATA_CACHE, key = "#type")
    public List<MasterDataDTO> getMasterDataByType(String type) {
        log.info("Fetching master data from DB for type: {}", type);
        return masterDataRepository.findByDataType(type).stream()
                .map(masterDataMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.MASTER_DATA_CACHE, allEntries = true)
    public Map<String, Object> bulkUploadMasterData(String type, MultipartFile file) {
        // 1. File Type and Empty Validation
        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/csv")) {
            throw new ValidationException("Invalid file type. Please upload a CSV file.");
        }

        List<Map<String, String>> parsedData = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> codesInCsv = new HashSet<>();

        // 2. Parsing and Header Validation
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ValidationException("CSV file is empty");
            }
            String[] headers = Arrays.stream(headerLine.split(",")).map(String::trim).toArray(String[]::new);

            if (!Arrays.asList(headers).containsAll(Arrays.asList("code", "value"))) {
                throw new ValidationException("CSV file must contain 'code' and 'value' headers.");
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                String[] values = line.split(",", -1); // Use -1 to include trailing empty strings
                if (values.length != headers.length) {
                    errors.add(createErrorMap(rowNum, "row",
                            "Invalid number of columns. Expected " + headers.length + ", but got " + values.length));
                    continue;
                }

                Map<String, String> rowMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    rowMap.put(headers[i], values[i].trim());
                }
                parsedData.add(rowMap);

                // 3. Duplicate Code Check within CSV
                String code = rowMap.get("code");
                if (code != null && !code.isEmpty()) {
                    if (!codesInCsv.add(code)) {
                        errors.add(
                                createErrorMap(rowNum, "code", "Duplicate code '" + code + "' found in the CSV file."));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", e.getMessage());
            throw new BusinessException("Error parsing CSV file: " + e.getMessage());
        }

        int totalRecords = parsedData.size();
        List<MasterData> recordsToSave = new ArrayList<>();

        // 4. Data Validation and Processing
        for (int i = 0; i < totalRecords; i++) {
            Map<String, String> rowData = parsedData.get(i);
            int rowNum = i + 2; // +1 for 0-based index, +1 for header row

            String code = rowData.get("code");
            String value = rowData.get("value");

            if (code == null || code.isEmpty()) {
                errors.add(createErrorMap(rowNum, "code", "Code cannot be empty"));
                continue;
            }
            if (value == null || value.isEmpty()) {
                errors.add(createErrorMap(rowNum, "value", "Value cannot be empty"));
                continue;
            }

            if (masterDataRepository.existsByDataTypeAndCode(type, code)) {
                errors.add(createErrorMap(rowNum, "code",
                        "Master data with type '" + type + "' and code '" + code + "' already exists"));
                continue;
            }

            MasterData masterData = new MasterData();
            masterData.setDataType(type);
            masterData.setCode(code);
            masterData.setValue(value);
            masterData.setParentCode(rowData.get("parentCode"));

            // Data Type Validation for isActive
            String isActiveStr = rowData.getOrDefault("isActive", "true");
            if (!isActiveStr.equalsIgnoreCase("true") && !isActiveStr.equalsIgnoreCase("false")
                    && !isActiveStr.isEmpty()) {
                errors.add(createErrorMap(rowNum, "isActive", "Invalid value. Must be 'true' or 'false'."));
                continue;
            }
            masterData.setIsActive(Boolean.parseBoolean(isActiveStr));

            // Data Type Validation for displayOrder
            String displayOrderStr = rowData.getOrDefault("displayOrder", "0");
            try {
                masterData.setDisplayOrder(Integer.parseInt(displayOrderStr));
            } catch (NumberFormatException e) {
                errors.add(createErrorMap(rowNum, "displayOrder", "Invalid value. Must be a valid number."));
                continue;
            }

            String metadataStr = rowData.get("metadata");
            if (metadataStr != null && !metadataStr.trim().isEmpty()) {
                masterData.setMetadata(metadataStr);
            }
            recordsToSave.add(masterData);
        }

        if (!recordsToSave.isEmpty()) {
            masterDataRepository.saveAll(recordsToSave);
        }

        int successCount = recordsToSave.size();

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", "upload_" + System.currentTimeMillis());
        result.put("totalRecords", totalRecords);
        result.put("successCount", successCount);
        result.put("failureCount", totalRecords - successCount);
        result.put("errors", errors);
        result.put("message", "Master data bulk upload processed.");

        return result;
    }

    private Map<String, Object> createErrorMap(int row, String field, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("row", row);
        error.put("field", field);
        error.put("message", message);
        return error;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.MASTER_DATA_CACHE, allEntries = true)
    public Map<String, Object> bulkUploadMasterDataV2(MultipartFile file) {
        // 1. File Type and Empty Validation
        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/csv")) {
            throw new ValidationException("Invalid file type. Please upload a CSV file.");
        }

        List<Map<String, String>> parsedData = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> codesInCsv = new HashSet<>();

        // 2. Parsing and Header Validation
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ValidationException("CSV file is empty");
            }
            String[] headers = Arrays.stream(headerLine.split(",")).map(String::trim).toArray(String[]::new);

            if (!Arrays.asList(headers).containsAll(Arrays.asList("categoryType", "code", "value"))) {
                throw new ValidationException("CSV file must contain 'categoryType', 'code' and 'value' headers.");
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                String[] values = line.split(",", -1); // Use -1 to include trailing empty strings
                if (values.length != headers.length) {
                    errors.add(createErrorMap(rowNum, "row",
                            "Invalid number of columns. Expected " + headers.length + ", but got " + values.length));
                    continue;
                }

                Map<String, String> rowMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    rowMap.put(headers[i], values[i].trim());
                }
                parsedData.add(rowMap);

                // 3. Duplicate Code Check within CSV
                String categoryType = rowMap.get("categoryType");
                String code = rowMap.get("code");
                if (categoryType != null && !categoryType.isEmpty() && code != null && !code.isEmpty()) {
                    String uniqueKey = categoryType + ":" + code;
                    if (!codesInCsv.add(uniqueKey)) {
                        errors.add(createErrorMap(rowNum, "code", "Duplicate code '" + code + "' for categoryType '"
                                + categoryType + "' found in the CSV file."));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", e.getMessage());
            throw new BusinessException("Error parsing CSV file: " + e.getMessage());
        }

        int totalRecords = parsedData.size();
        List<MasterData> recordsToSave = new ArrayList<>();

        // 4. Data Validation and Processing
        for (int i = 0; i < totalRecords; i++) {
            Map<String, String> rowData = parsedData.get(i);
            int rowNum = i + 2; // +1 for 0-based index, +1 for header row

            String categoryType = rowData.get("categoryType");
            String code = rowData.get("code");
            String value = rowData.get("value");

            if (categoryType == null || categoryType.isEmpty()) {
                errors.add(createErrorMap(rowNum, "categoryType", "CategoryType cannot be empty"));
                continue;
            }
            if (code == null || code.isEmpty()) {
                errors.add(createErrorMap(rowNum, "code", "Code cannot be empty"));
                continue;
            }
            if (value == null || value.isEmpty()) {
                errors.add(createErrorMap(rowNum, "value", "Value cannot be empty"));
                continue;
            }

            if (masterDataRepository.existsByDataTypeAndCode(categoryType, code)) {
                errors.add(createErrorMap(rowNum, "code",
                        "Master data with type '" + categoryType + "' and code '" + code + "' already exists"));
                continue;
            }

            MasterData masterData = new MasterData();
            masterData.setDataType(categoryType);
            masterData.setCode(code);
            masterData.setValue(value);
            masterData.setParentCode(rowData.get("parentCode"));

            // Data Type Validation for isActive
            String isActiveStr = rowData.getOrDefault("isActive", "true");
            if (!isActiveStr.equalsIgnoreCase("true") && !isActiveStr.equalsIgnoreCase("false")
                    && !isActiveStr.isEmpty()) {
                errors.add(createErrorMap(rowNum, "isActive", "Invalid value. Must be 'true' or 'false'."));
                continue;
            }
            masterData.setIsActive(Boolean.parseBoolean(isActiveStr));

            // Data Type Validation for displayOrder
            String displayOrderStr = rowData.getOrDefault("displayOrder", "0");
            try {
                masterData.setDisplayOrder(Integer.parseInt(displayOrderStr));
            } catch (NumberFormatException e) {
                errors.add(createErrorMap(rowNum, "displayOrder", "Invalid value. Must be a valid number."));
                continue;
            }

            String metadataStr = rowData.get("metadata");
            if (metadataStr != null && !metadataStr.trim().isEmpty()) {
                masterData.setMetadata(metadataStr);
            }
            recordsToSave.add(masterData);
        }

        if (!recordsToSave.isEmpty()) {
            masterDataRepository.saveAll(recordsToSave);
        }

        int successCount = recordsToSave.size();

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", "upload_" + System.currentTimeMillis());
        result.put("totalRecords", totalRecords);
        result.put("successCount", successCount);
        result.put("failureCount", totalRecords - successCount);
        result.put("errors", errors);
        result.put("message", "Master data bulk upload processed.");

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.MASTER_DATA_CACHE, allEntries = true)
    @SuppressWarnings("null")
    public void deleteMasterData(Long id) {
        if (!masterDataRepository.existsById(id)) {
            throw new BusinessException("Master data with id '" + id + "' not found.");
        }
        masterDataRepository.deleteById(id);
    }

    @Override
    @SuppressWarnings("null")
    @Transactional
    @CacheEvict(value = CacheConstants.MASTER_DATA_CACHE, allEntries = true)
    public MasterDataDTO updateMasterData(Long id, MasterDataDTO masterDataDTO) {
        MasterData existingMasterData = masterDataRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Master data with id '" + id + "' not found."));

        // Update fields
        existingMasterData.setValue(masterDataDTO.getValue());
        existingMasterData.setParentCode(masterDataDTO.getParentCode());
        existingMasterData.setIsActive(masterDataDTO.getIsActive());
        existingMasterData.setDisplayOrder(masterDataDTO.getDisplayOrder());
        existingMasterData.setMetadata(masterDataDTO.getMetadata());

        MasterData updatedMasterData = masterDataRepository.save(existingMasterData);
        return masterDataMapper.toDto(updatedMasterData);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.MASTER_DATA_CACHE, allEntries = true)
    public void deleteMasterDataByType(String type) {
        if (!masterDataRepository.existsByDataType(type)) {
            throw new BusinessException("Master data with type '" + type + "' not found.");
        }
        masterDataRepository.deleteByDataType(type);
    }
}
