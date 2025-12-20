package com.finx.configurationsservice.domain.dto;

import com.finx.configurationsservice.domain.enums.HolidayType;
import com.finx.configurationsservice.domain.enums.RescheduleStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDTO {
    private Long id;
    private String holidayCode;
    private String holidayName;
    private HolidayType holidayType;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> applicableStates;
    private List<Long> applicableOffices;
    private RescheduleStrategy rescheduleStrategy;
    private LocalDate fixedReplacementDate;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
