package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.AuthType;
import com.finx.configurationsservice.domain.enums.LmsType;
import com.finx.configurationsservice.domain.enums.SyncFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLmsConfigRequest {

    @NotBlank(message = "LMS code is required")
    @Size(max = 50, message = "LMS code must not exceed 50 characters")
    private String lmsCode;

    @NotBlank(message = "LMS name is required")
    @Size(max = 200, message = "LMS name must not exceed 200 characters")
    private String lmsName;

    @NotNull(message = "LMS type is required")
    private LmsType lmsType;

    private String description;

    // Connection Details
    private String connectionUrl;
    private String databaseName;
    private String schemaName;
    private String username;
    private String password;

    // API Configuration
    private String apiEndpoint;
    private AuthType apiAuthType;
    private String apiKey;
    private String apiSecret;

    // File Configuration
    private String fileLocation;
    private String fileFormat;
    private String fileDelimiter;
    private String fileEncoding;

    // Sync Configuration
    private SyncFrequency syncFrequency;
    private String syncSchedule;
    private LocalTime syncStartTime;
    private Integer batchSize;

    // Field Mappings
    private Map<String, String> fieldMappings;

    // Identifiers
    private String lmsIdentifier;
    private String paymentTypeId;
    private List<String> productTypes;

    // Status
    private Boolean isActive;
}
