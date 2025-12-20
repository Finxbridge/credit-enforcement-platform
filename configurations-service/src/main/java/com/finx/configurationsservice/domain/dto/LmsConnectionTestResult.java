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
public class LmsConnectionTestResult {
    private Long lmsId;
    private String lmsCode;
    private String lmsName;
    private TestStatus testStatus;
    private Integer responseTimeMs;
    private String testMessage;
    private String errorDetails;
    private LocalDateTime testedAt;
    private Long testedBy;
}
