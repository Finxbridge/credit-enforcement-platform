package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.OfficeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfficeRequest {

    private Long organizationId;

    // officeCode is now auto-generated in backend, not required from client
    private String officeCode;

    @NotBlank(message = "Office name is required")
    private String officeName;

    @NotNull(message = "Office type is required")
    private OfficeType officeType;

    private Long parentOfficeId;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String phone;

    private String email;

    private LocalDate openingDate;

    private Long workCalendarId;

    private Long createdBy;
}
