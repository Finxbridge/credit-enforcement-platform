package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreateWorkCalendarRequest;
import com.finx.configurationsservice.domain.dto.WorkCalendarDTO;
import com.finx.configurationsservice.service.WorkCalendarService;
import com.finx.configurationsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/config/work-calendars")
@RequiredArgsConstructor
public class WorkCalendarController {

    private final WorkCalendarService workCalendarService;

    @PostMapping
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> createWorkCalendar(
            @Valid @RequestBody CreateWorkCalendarRequest request) {
        WorkCalendarDTO calendar = workCalendarService.createWorkCalendar(request);
        return ResponseWrapper.created("Work calendar created successfully", calendar);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> getWorkCalendarById(@PathVariable Long id) {
        WorkCalendarDTO calendar = workCalendarService.getWorkCalendarById(id);
        return ResponseWrapper.ok("Work calendar retrieved successfully", calendar);
    }

    @GetMapping("/code/{calendarCode}")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> getWorkCalendarByCode(@PathVariable String calendarCode) {
        WorkCalendarDTO calendar = workCalendarService.getWorkCalendarByCode(calendarCode);
        return ResponseWrapper.ok("Work calendar retrieved successfully", calendar);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<WorkCalendarDTO>>> getActiveWorkCalendars() {
        List<WorkCalendarDTO> calendars = workCalendarService.getActiveWorkCalendars();
        return ResponseWrapper.ok("Active work calendars retrieved successfully", calendars);
    }

    @GetMapping("/default")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> getDefaultWorkCalendar() {
        WorkCalendarDTO calendar = workCalendarService.getDefaultWorkCalendar();
        return ResponseWrapper.ok("Default work calendar retrieved successfully", calendar);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> updateWorkCalendar(
            @PathVariable Long id,
            @Valid @RequestBody CreateWorkCalendarRequest request) {
        WorkCalendarDTO calendar = workCalendarService.updateWorkCalendar(id, request);
        return ResponseWrapper.ok("Work calendar updated successfully", calendar);
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> setAsDefault(@PathVariable Long id) {
        WorkCalendarDTO calendar = workCalendarService.setAsDefault(id);
        return ResponseWrapper.ok("Work calendar set as default successfully", calendar);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> activateWorkCalendar(@PathVariable Long id) {
        WorkCalendarDTO calendar = workCalendarService.activateWorkCalendar(id);
        return ResponseWrapper.ok("Work calendar activated successfully", calendar);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<WorkCalendarDTO>> deactivateWorkCalendar(@PathVariable Long id) {
        WorkCalendarDTO calendar = workCalendarService.deactivateWorkCalendar(id);
        return ResponseWrapper.ok("Work calendar deactivated successfully", calendar);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteWorkCalendar(@PathVariable Long id) {
        workCalendarService.deleteWorkCalendar(id);
        return ResponseWrapper.okMessage("Work calendar deleted successfully");
    }

    @GetMapping("/{calendarId}/is-working-day/{date}")
    public ResponseEntity<CommonResponse<Boolean>> isWorkingDay(
            @PathVariable Long calendarId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean result = workCalendarService.isWorkingDay(calendarId, date);
        return ResponseWrapper.ok("Working day check completed", result);
    }

    @GetMapping("/{calendarId}/next-working-day/{date}")
    public ResponseEntity<CommonResponse<LocalDate>> getNextWorkingDay(
            @PathVariable Long calendarId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate nextWorkingDay = workCalendarService.getNextWorkingDay(calendarId, date);
        return ResponseWrapper.ok("Next working day calculated", nextWorkingDay);
    }
}
