package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.RescheduleStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkCalendarRequest {

    @NotBlank(message = "Calendar code is required")
    private String calendarCode;

    @NotBlank(message = "Calendar name is required")
    private String calendarName;

    @NotNull(message = "Working days configuration is required")
    private Map<String, Boolean> workingDays;

    private LocalTime workStartTime;

    private LocalTime workEndTime;

    private LocalTime breakStartTime;

    private LocalTime breakEndTime;

    private RescheduleStrategy nonWorkingDayBehavior;

    private Boolean isDefault;

    private Long createdBy;
}
