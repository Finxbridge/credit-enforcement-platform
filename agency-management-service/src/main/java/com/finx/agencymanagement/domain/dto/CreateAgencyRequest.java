package com.finx.agencymanagement.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finx.agencymanagement.domain.enums.AgencyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create Agency Request DTO
 * Matches frontend agency.types.ts CreateAgencyRequest interface exactly
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAgencyRequest {

    @NotBlank(message = "Agency name is required")
    private String agencyName;

    @NotNull(message = "Agency type is required")
    private AgencyType agencyType;

    // Contact Information
    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    // Address - Frontend uses single 'address' field (optional)
    private String address;
    private String city;
    private String state;
    private String pincode;

    // KYC Documents (optional in frontend)
    private String panNumber;
    private String gstNumber;

    // Bank Details (optional in frontend)
    private String bankAccountNumber;
    private String bankName;
    @JsonProperty("ifscCode")
    private String ifscCode;

    // Contract Details (optional in frontend)
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    @JsonProperty("commissionRate")
    private BigDecimal commissionRate;
    @JsonProperty("maxCaseLimit")
    private Integer maxCaseLimit;

    // Notes (optional)
    private String notes;

    // Optional fields - sent as JSON strings from frontend
    // KYC Documents - JSON array of {documentType, documentName, documentUrl}
    private String kycDocuments;

    // Service areas - JSON array of strings (e.g., ["Mumbai", "Delhi", "Bangalore"])
    private String serviceAreas;

    // Service pincodes - JSON array of strings (e.g., ["500001", "500002"])
    private String servicePincodes;
}
