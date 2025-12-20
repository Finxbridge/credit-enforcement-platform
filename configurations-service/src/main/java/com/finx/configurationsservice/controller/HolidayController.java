package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreateHolidayRequest;
import com.finx.configurationsservice.domain.dto.HolidayDTO;
import com.finx.configurationsservice.domain.enums.HolidayType;
import com.finx.configurationsservice.service.HolidayService;
import com.finx.configurationsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/config/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    public ResponseEntity<CommonResponse<HolidayDTO>> createHoliday(
            @Valid @RequestBody CreateHolidayRequest request) {
        HolidayDTO holiday = holidayService.createHoliday(request);
        return ResponseWrapper.created("Holiday created successfully", holiday);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<HolidayDTO>> getHolidayById(@PathVariable Long id) {
        HolidayDTO holiday = holidayService.getHolidayById(id);
        return ResponseWrapper.ok("Holiday retrieved successfully", holiday);
    }

    @GetMapping("/code/{holidayCode}")
    public ResponseEntity<CommonResponse<HolidayDTO>> getHolidayByCode(@PathVariable String holidayCode) {
        HolidayDTO holiday = holidayService.getHolidayByCode(holidayCode);
        return ResponseWrapper.ok("Holiday retrieved successfully", holiday);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<HolidayDTO>>> getActiveHolidays() {
        List<HolidayDTO> holidays = holidayService.getActiveHolidays();
        return ResponseWrapper.ok("Active holidays retrieved successfully", holidays);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<List<HolidayDTO>>> getHolidaysByType(@PathVariable HolidayType type) {
        List<HolidayDTO> holidays = holidayService.getHolidaysByType(type);
        return ResponseWrapper.ok("Holidays retrieved successfully", holidays);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<CommonResponse<List<HolidayDTO>>> getHolidaysForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<HolidayDTO> holidays = holidayService.getHolidaysForDate(date);
        return ResponseWrapper.ok("Holidays retrieved successfully", holidays);
    }

    @GetMapping("/range")
    public ResponseEntity<CommonResponse<List<HolidayDTO>>> getHolidaysBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<HolidayDTO> holidays = holidayService.getHolidaysBetweenDates(startDate, endDate);
        return ResponseWrapper.ok("Holidays retrieved successfully", holidays);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<CommonResponse<List<HolidayDTO>>> getHolidaysByYear(@PathVariable int year) {
        List<HolidayDTO> holidays = holidayService.getHolidaysByYear(year);
        return ResponseWrapper.ok("Holidays retrieved successfully", holidays);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<HolidayDTO>>> getAllHolidays(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<HolidayDTO> holidays = holidayService.getAllHolidays(pageable);
        return ResponseWrapper.ok("Holidays retrieved successfully", holidays);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<HolidayDTO>> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody CreateHolidayRequest request) {
        HolidayDTO holiday = holidayService.updateHoliday(id, request);
        return ResponseWrapper.ok("Holiday updated successfully", holiday);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<HolidayDTO>> activateHoliday(@PathVariable Long id) {
        HolidayDTO holiday = holidayService.activateHoliday(id);
        return ResponseWrapper.ok("Holiday activated successfully", holiday);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<HolidayDTO>> deactivateHoliday(@PathVariable Long id) {
        HolidayDTO holiday = holidayService.deactivateHoliday(id);
        return ResponseWrapper.ok("Holiday deactivated successfully", holiday);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseWrapper.okMessage("Holiday deleted successfully");
    }

    @GetMapping("/check/{date}")
    public ResponseEntity<CommonResponse<Boolean>> isHoliday(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean result = holidayService.isHoliday(date);
        return ResponseWrapper.ok("Holiday check completed", result);
    }
}
