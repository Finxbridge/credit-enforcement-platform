package com.finx.configurationsservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    private Long id;
    private String orgCode;
    private String orgName;
    private String legalName;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String email;
    private String phone;
    private String website;
    private String address;
    private String defaultCurrency;
    private String defaultLanguage;
    private String defaultTimezone;
    private String dateFormat;
    private String licenseType;
    private LocalDate licenseValidUntil;
    private Integer maxUsers;
    private Integer maxCases;
    private Map<String, Boolean> enabledFeatures;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
