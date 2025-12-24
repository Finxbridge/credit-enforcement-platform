package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.OfficeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeDTO {
    private Long id;
    private Long organizationId;
    private String officeCode;
    private String officeName;
    private OfficeType officeType;
    private Long parentOfficeId;
    private String parentOfficeName;
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
    private String workCalendarName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
