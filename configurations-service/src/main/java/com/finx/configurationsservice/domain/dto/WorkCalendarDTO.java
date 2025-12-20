package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.RescheduleStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCalendarDTO {
    private Long id;
    private String calendarCode;
    private String calendarName;
    private Map<String, Boolean> workingDays;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private RescheduleStrategy nonWorkingDayBehavior;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
