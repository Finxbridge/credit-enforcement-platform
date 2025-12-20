package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.config.CacheConstants;
import com.finx.configurationsservice.domain.dto.CreateWorkCalendarRequest;
import com.finx.configurationsservice.domain.dto.WorkCalendarDTO;
import com.finx.configurationsservice.domain.entity.WorkCalendar;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.HolidayRepository;
import com.finx.configurationsservice.repository.WorkCalendarRepository;
import com.finx.configurationsservice.service.WorkCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkCalendarServiceImpl implements WorkCalendarService {

    private final WorkCalendarRepository workCalendarRepository;
    private final HolidayRepository holidayRepository;
    private final ConfigurationsMapper mapper;

    @Override
    @CacheEvict(value = CacheConstants.WORK_CALENDAR_CACHE, allEntries = true)
    public WorkCalendarDTO createWorkCalendar(CreateWorkCalendarRequest request) {
        log.info("Creating work calendar: {}", request.getCalendarCode());

        if (workCalendarRepository.existsByCalendarCode(request.getCalendarCode())) {
            throw new BusinessException("Calendar code already exists: " + request.getCalendarCode());
        }

        WorkCalendar calendar = mapper.toEntity(request);
        calendar.setIsActive(true);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearExistingDefault();
            calendar.setIsDefault(true);
        }

        WorkCalendar savedCalendar = workCalendarRepository.save(calendar);
        log.info("Work calendar created with ID: {}", savedCalendar.getId());

        return mapper.toDto(savedCalendar);
    }

    @Override
    @Cacheable(value = CacheConstants.WORK_CALENDAR_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public WorkCalendarDTO getWorkCalendarById(Long id) {
        WorkCalendar calendar = workCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", id));
        return mapper.toDto(calendar);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkCalendarDTO getWorkCalendarByCode(String calendarCode) {
        WorkCalendar calendar = workCalendarRepository.findByCalendarCode(calendarCode)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", calendarCode));
        return mapper.toDto(calendar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkCalendarDTO> getActiveWorkCalendars() {
        return mapper.toWorkCalendarDtoList(workCalendarRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkCalendarDTO getDefaultWorkCalendar() {
        WorkCalendar calendar = workCalendarRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Default WorkCalendar not configured"));
        return mapper.toDto(calendar);
    }

    @Override
    @CacheEvict(value = CacheConstants.WORK_CALENDAR_CACHE, allEntries = true)
    public WorkCalendarDTO updateWorkCalendar(Long id, CreateWorkCalendarRequest request) {
        log.info("Updating work calendar ID: {}", id);

        WorkCalendar calendar = workCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", id));

        calendar.setCalendarName(request.getCalendarName());
        calendar.setWorkingDays(request.getWorkingDays());
        calendar.setWorkStartTime(request.getWorkStartTime());
        calendar.setWorkEndTime(request.getWorkEndTime());
        calendar.setBreakStartTime(request.getBreakStartTime());
        calendar.setBreakEndTime(request.getBreakEndTime());
        calendar.setNonWorkingDayBehavior(request.getNonWorkingDayBehavior());

        return mapper.toDto(workCalendarRepository.save(calendar));
    }

    @Override
    @CacheEvict(value = CacheConstants.WORK_CALENDAR_CACHE, allEntries = true)
    public WorkCalendarDTO setAsDefault(Long id) {
        WorkCalendar calendar = workCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", id));

        clearExistingDefault();
        calendar.setIsDefault(true);

        return mapper.toDto(workCalendarRepository.save(calendar));
    }

    @Override
    @CacheEvict(value = CacheConstants.WORK_CALENDAR_CACHE, allEntries = true)
    public WorkCalendarDTO activateWorkCalendar(Long id) {
        WorkCalendar calendar = workCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", id));
        calendar.setIsActive(true);
        return mapper.toDto(workCalendarRepository.save(calendar));
    }

    @Override
    @CacheEvict(value = CacheConstants.WORK_CALENDAR_CACHE, allEntries = true)
    public WorkCalendarDTO deactivateWorkCalendar(Long id) {
        WorkCalendar calendar = workCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", id));
        calendar.setIsActive(false);
        return mapper.toDto(workCalendarRepository.save(calendar));
    }

    @Override
    @CacheEvict(value = CacheConstants.WORK_CALENDAR_CACHE, allEntries = true)
    public void deleteWorkCalendar(Long id) {
        if (!workCalendarRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkCalendar", id);
        }
        workCalendarRepository.deleteById(id);
        log.info("Work calendar deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWorkingDay(Long calendarId, LocalDate date) {
        WorkCalendar calendar = workCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", calendarId));

        // Check if it's a holiday
        if (!holidayRepository.findHolidaysForDate(date).isEmpty()) {
            return false;
        }

        // Check if it's a working day according to the calendar
        String dayOfWeek = date.getDayOfWeek().name().toLowerCase();
        Map<String, Boolean> workingDays = calendar.getWorkingDays();

        return workingDays != null && Boolean.TRUE.equals(workingDays.get(dayOfWeek));
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDate getNextWorkingDay(Long calendarId, LocalDate date) {
        LocalDate nextDate = date.plusDays(1);
        int maxIterations = 30; // Prevent infinite loop
        int iterations = 0;

        while (!isWorkingDay(calendarId, nextDate) && iterations < maxIterations) {
            nextDate = nextDate.plusDays(1);
            iterations++;
        }

        return nextDate;
    }

    private void clearExistingDefault() {
        workCalendarRepository.findByIsDefaultTrue().ifPresent(existing -> {
            existing.setIsDefault(false);
            workCalendarRepository.save(existing);
        });
    }
}
