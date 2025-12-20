package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.config.CacheConstants;
import com.finx.configurationsservice.domain.dto.CreateHolidayRequest;
import com.finx.configurationsservice.domain.dto.HolidayDTO;
import com.finx.configurationsservice.domain.entity.Holiday;
import com.finx.configurationsservice.domain.enums.HolidayType;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.HolidayRepository;
import com.finx.configurationsservice.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final ConfigurationsMapper mapper;

    @Override
    @CacheEvict(value = {CacheConstants.HOLIDAY_CACHE, CacheConstants.HOLIDAY_LIST_CACHE}, allEntries = true)
    public HolidayDTO createHoliday(CreateHolidayRequest request) {
        log.info("Creating holiday: {}", request.getHolidayCode());

        if (holidayRepository.existsByHolidayCode(request.getHolidayCode())) {
            throw new BusinessException("Holiday code already exists: " + request.getHolidayCode());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        Holiday holiday = mapper.toEntity(request);
        holiday.setIsActive(true);

        Holiday savedHoliday = holidayRepository.save(holiday);
        log.info("Holiday created with ID: {}", savedHoliday.getId());

        return mapper.toDto(savedHoliday);
    }

    @Override
    @Cacheable(value = CacheConstants.HOLIDAY_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public HolidayDTO getHolidayById(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));
        return mapper.toDto(holiday);
    }

    @Override
    @Transactional(readOnly = true)
    public HolidayDTO getHolidayByCode(String holidayCode) {
        Holiday holiday = holidayRepository.findByHolidayCode(holidayCode)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", holidayCode));
        return mapper.toDto(holiday);
    }

    @Override
    @Cacheable(value = CacheConstants.HOLIDAY_LIST_CACHE, key = "'active'")
    @Transactional(readOnly = true)
    public List<HolidayDTO> getActiveHolidays() {
        return mapper.toHolidayDtoList(holidayRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayDTO> getHolidaysByType(HolidayType type) {
        return mapper.toHolidayDtoList(holidayRepository.findByHolidayType(type));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayDTO> getHolidaysForDate(LocalDate date) {
        return mapper.toHolidayDtoList(holidayRepository.findHolidaysForDate(date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayDTO> getHolidaysBetweenDates(LocalDate startDate, LocalDate endDate) {
        return mapper.toHolidayDtoList(holidayRepository.findHolidaysBetweenDates(startDate, endDate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayDTO> getHolidaysByYear(int year) {
        return mapper.toHolidayDtoList(holidayRepository.findHolidaysByYear(year));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HolidayDTO> getAllHolidays(Pageable pageable) {
        return holidayRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @CacheEvict(value = {CacheConstants.HOLIDAY_CACHE, CacheConstants.HOLIDAY_LIST_CACHE}, allEntries = true)
    public HolidayDTO updateHoliday(Long id, CreateHolidayRequest request) {
        log.info("Updating holiday ID: {}", id);

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));

        holiday.setHolidayName(request.getHolidayName());
        holiday.setHolidayType(request.getHolidayType());
        holiday.setStartDate(request.getStartDate());
        holiday.setEndDate(request.getEndDate());
        holiday.setApplicableStates(request.getApplicableStates());
        holiday.setApplicableOffices(request.getApplicableOffices());
        holiday.setRescheduleStrategy(request.getRescheduleStrategy());
        holiday.setFixedReplacementDate(request.getFixedReplacementDate());
        holiday.setDescription(request.getDescription());

        return mapper.toDto(holidayRepository.save(holiday));
    }

    @Override
    @CacheEvict(value = {CacheConstants.HOLIDAY_CACHE, CacheConstants.HOLIDAY_LIST_CACHE}, allEntries = true)
    public HolidayDTO activateHoliday(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));
        holiday.setIsActive(true);
        return mapper.toDto(holidayRepository.save(holiday));
    }

    @Override
    @CacheEvict(value = {CacheConstants.HOLIDAY_CACHE, CacheConstants.HOLIDAY_LIST_CACHE}, allEntries = true)
    public HolidayDTO deactivateHoliday(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));
        holiday.setIsActive(false);
        return mapper.toDto(holidayRepository.save(holiday));
    }

    @Override
    @CacheEvict(value = {CacheConstants.HOLIDAY_CACHE, CacheConstants.HOLIDAY_LIST_CACHE}, allEntries = true)
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Holiday", id);
        }
        holidayRepository.deleteById(id);
        log.info("Holiday deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHoliday(LocalDate date) {
        List<Holiday> holidays = holidayRepository.findHolidaysForDate(date);
        return !holidays.isEmpty();
    }
}
