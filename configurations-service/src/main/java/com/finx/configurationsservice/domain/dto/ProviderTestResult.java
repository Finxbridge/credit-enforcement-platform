package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderTestResult {
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String testType;
    private TestStatus testStatus;
    private Integer responseTimeMs;
    private Integer responseCode;
    private String responseMessage;
    private String errorDetails;
    private LocalDateTime testedAt;
    private Long testedBy;
}
