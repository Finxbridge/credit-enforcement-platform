package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.CreateWorkCalendarRequest;
import com.finx.configurationsservice.domain.dto.WorkCalendarDTO;

import java.time.LocalDate;
import java.util.List;

public interface WorkCalendarService {

    WorkCalendarDTO createWorkCalendar(CreateWorkCalendarRequest request);

    WorkCalendarDTO getWorkCalendarById(Long id);

    WorkCalendarDTO getWorkCalendarByCode(String calendarCode);

    List<WorkCalendarDTO> getActiveWorkCalendars();

    WorkCalendarDTO getDefaultWorkCalendar();

    WorkCalendarDTO updateWorkCalendar(Long id, CreateWorkCalendarRequest request);

    WorkCalendarDTO setAsDefault(Long id);

    WorkCalendarDTO activateWorkCalendar(Long id);

    WorkCalendarDTO deactivateWorkCalendar(Long id);

    void deleteWorkCalendar(Long id);

    boolean isWorkingDay(Long calendarId, LocalDate date);

    LocalDate getNextWorkingDay(Long calendarId, LocalDate date);
}
