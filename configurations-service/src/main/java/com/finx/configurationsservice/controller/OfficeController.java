package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreateOfficeRequest;
import com.finx.configurationsservice.domain.dto.OfficeDTO;
import com.finx.configurationsservice.domain.enums.OfficeType;
import com.finx.configurationsservice.service.OfficeService;
import com.finx.configurationsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @PostMapping
    public ResponseEntity<CommonResponse<OfficeDTO>> createOffice(
            @Valid @RequestBody CreateOfficeRequest request) {
        OfficeDTO office = officeService.createOffice(request);
        return ResponseWrapper.created("Office created successfully", office);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<OfficeDTO>> getOfficeById(@PathVariable Long id) {
        OfficeDTO office = officeService.getOfficeById(id);
        return ResponseWrapper.ok("Office retrieved successfully", office);
    }

    @GetMapping("/code/{officeCode}")
    public ResponseEntity<CommonResponse<OfficeDTO>> getOfficeByCode(@PathVariable String officeCode) {
        OfficeDTO office = officeService.getOfficeByCode(officeCode);
        return ResponseWrapper.ok("Office retrieved successfully", office);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<OfficeDTO>>> getActiveOffices() {
        List<OfficeDTO> offices = officeService.getActiveOffices();
        return ResponseWrapper.ok("Active offices retrieved successfully", offices);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<CommonResponse<List<OfficeDTO>>> getOfficesByType(@PathVariable OfficeType type) {
        List<OfficeDTO> offices = officeService.getOfficesByType(type);
        return ResponseWrapper.ok("Offices retrieved successfully", offices);
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<CommonResponse<List<OfficeDTO>>> getOfficesByState(@PathVariable String state) {
        List<OfficeDTO> offices = officeService.getOfficesByState(state);
        return ResponseWrapper.ok("Offices retrieved successfully", offices);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<CommonResponse<List<OfficeDTO>>> getChildOffices(@PathVariable Long parentId) {
        List<OfficeDTO> offices = officeService.getChildOffices(parentId);
        return ResponseWrapper.ok("Child offices retrieved successfully", offices);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<OfficeDTO>>> getAllOffices(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OfficeDTO> offices = officeService.getAllOffices(pageable);
        return ResponseWrapper.ok("Offices retrieved successfully", offices);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<OfficeDTO>> updateOffice(
            @PathVariable Long id,
            @Valid @RequestBody CreateOfficeRequest request) {
        OfficeDTO office = officeService.updateOffice(id, request);
        return ResponseWrapper.ok("Office updated successfully", office);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<OfficeDTO>> activateOffice(@PathVariable Long id) {
        OfficeDTO office = officeService.activateOffice(id);
        return ResponseWrapper.ok("Office activated successfully", office);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<OfficeDTO>> deactivateOffice(@PathVariable Long id) {
        OfficeDTO office = officeService.deactivateOffice(id);
        return ResponseWrapper.ok("Office deactivated successfully", office);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteOffice(@PathVariable Long id) {
        officeService.deleteOffice(id);
        return ResponseWrapper.okMessage("Office deleted successfully");
    }
}
