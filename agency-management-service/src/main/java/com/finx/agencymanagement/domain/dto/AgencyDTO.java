package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.agencymanagement.domain.enums.AgencyStatus;
import com.finx.agencymanagement.domain.enums.AgencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Agency DTO
 * Matches frontend agency.types.ts Agency interface exactly
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyDTO {

    private Long id;
    private String agencyCode;
    private String agencyName;
    private AgencyType agencyType;
    private AgencyStatus status;

    // Contact Information
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;

    // Address - Frontend uses single 'address' field
    private String address;
    private String city;
    private String state;
    private String pincode;

    // KYC - Frontend field names
    private String panNumber;
    private String gstNumber;

    // Bank Details - Frontend field names
    private String bankAccountNumber;
    private String bankName;
    @JsonProperty("ifscCode")
    private String ifscCode;

    // Contract - Frontend field names
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    @JsonProperty("commissionRate")
    private BigDecimal commissionRate;
    @JsonProperty("maxCaseLimit")
    private Integer maxCaseLimit;
    @JsonProperty("currentCaseCount")
    private Integer currentCaseCount;

    // Approval info - Frontend field names
    private Long approvedBy;
    @JsonProperty("approvalDate")
    private LocalDateTime approvalDate;
    private String rejectionReason;
    private String notes;

    // Optional fields - parsed from JSONB
    private List<Map<String, Object>> kycDocuments;
    private List<String> serviceAreas;
    private List<String> servicePincodes;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
