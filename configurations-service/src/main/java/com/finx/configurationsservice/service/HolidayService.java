package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.CreateHolidayRequest;
import com.finx.configurationsservice.domain.dto.HolidayDTO;
import com.finx.configurationsservice.domain.enums.HolidayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {

    HolidayDTO createHoliday(CreateHolidayRequest request);

    HolidayDTO getHolidayById(Long id);

    HolidayDTO getHolidayByCode(String holidayCode);

    List<HolidayDTO> getActiveHolidays();

    List<HolidayDTO> getHolidaysByType(HolidayType type);

    List<HolidayDTO> getHolidaysForDate(LocalDate date);

    List<HolidayDTO> getHolidaysBetweenDates(LocalDate startDate, LocalDate endDate);

    List<HolidayDTO> getHolidaysByYear(int year);

    Page<HolidayDTO> getAllHolidays(Pageable pageable);

    HolidayDTO updateHoliday(Long id, CreateHolidayRequest request);

    HolidayDTO activateHoliday(Long id);

    HolidayDTO deactivateHoliday(Long id);

    void deleteHoliday(Long id);

    boolean isHoliday(LocalDate date);
}
