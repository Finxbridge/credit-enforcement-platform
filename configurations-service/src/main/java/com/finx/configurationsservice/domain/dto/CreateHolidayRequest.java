package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.HolidayType;
import com.finx.configurationsservice.domain.enums.RescheduleStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHolidayRequest {

    private Long organizationId;

    @NotBlank(message = "Holiday code is required")
    private String holidayCode;

    @NotBlank(message = "Holiday name is required")
    private String holidayName;

    @NotNull(message = "Holiday type is required")
    private HolidayType holidayType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private List<String> applicableStates;

    private List<Long> applicableOffices;

    private RescheduleStrategy rescheduleStrategy;

    private LocalDate fixedReplacementDate;

    private String description;

    private Long createdBy;
}
