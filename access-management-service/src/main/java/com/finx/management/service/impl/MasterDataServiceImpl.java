package com.finx.management.service.impl;

import com.finx.management.domain.dto.MasterDataDTO;
import com.finx.management.domain.entity.MasterData;
import com.finx.management.exception.BusinessException;
import com.finx.management.mapper.MasterDataMapper;
import com.finx.management.repository.MasterDataRepository;
import com.finx.management.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<MasterDataDTO> getMasterDataByType(String type) {
        return masterDataRepository.findByDataType(type).stream()
                .map(masterDataMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> bulkUploadMasterData(String type, MultipartFile file) {
        // 1. File Type and Empty Validation
        if (file.isEmpty()) {
            throw new BusinessException("File cannot be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/csv")) {
            throw new BusinessException("Invalid file type. Please upload a CSV file.");
        }

        List<Map<String, String>> parsedData = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> codesInCsv = new HashSet<>();

        // 2. Parsing and Header Validation
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException("CSV file is empty");
            }
            String[] headers = Arrays.stream(headerLine.split(",")).map(String::trim).toArray(String[]::new);

            if (!Arrays.asList(headers).containsAll(Arrays.asList("code", "value"))) {
                throw new BusinessException("CSV file must contain 'code' and 'value' headers.");
            }


            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                String[] values = line.split(",", -1); // Use -1 to include trailing empty strings
                if (values.length != headers.length) {
                    errors.add(createErrorMap(rowNum, "row", "Invalid number of columns. Expected " + headers.length + ", but got " + values.length));
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
                        errors.add(createErrorMap(rowNum, "code", "Duplicate code '" + code + "' found in the CSV file."));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", e.getMessage());
            throw new BusinessException("Error parsing CSV file: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("uploadId", "upload_" + System.currentTimeMillis());
            result.put("totalRecords", parsedData.size());
            result.put("successCount", 0);
            result.put("failureCount", parsedData.size());
            result.put("errors", errors);
            result.put("message", "CSV parsing failed. Please fix the errors and re-upload.");
            return result;
        }


        int totalRecords = parsedData.size();
        int successCount = 0;
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
            if (!isActiveStr.equalsIgnoreCase("true") && !isActiveStr.equalsIgnoreCase("false") && !isActiveStr.isEmpty()) {
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

            masterData.setMetadata(rowData.get("metadata"));
            recordsToSave.add(masterData);
        }

        if (errors.isEmpty()) {
            masterDataRepository.saveAll(recordsToSave);
            successCount = recordsToSave.size();
        } else {
             // Decide if you want to save partial data or fail the whole batch
            // Current implementation fails the whole batch if there are data validation errors.
        }


        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", "upload_" + System.currentTimeMillis());
        result.put("totalRecords", totalRecords);
        result.put("successCount", successCount);
        result.put("failureCount", errors.size());
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
}
