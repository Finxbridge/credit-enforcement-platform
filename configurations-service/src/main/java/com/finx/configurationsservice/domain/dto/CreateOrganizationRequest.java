package com.finx.configurationsservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization code is required")
    @Size(max = 50, message = "Organization code must be at most 50 characters")
    private String orgCode;

    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must be at most 255 characters")
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
}
