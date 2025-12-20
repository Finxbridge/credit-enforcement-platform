package com.finx.noticemanagementservice.controller;

import com.finx.noticemanagementservice.domain.dto.CommonResponse;
import com.finx.noticemanagementservice.domain.dto.CreateVendorRequest;
import com.finx.noticemanagementservice.domain.dto.NoticeVendorDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateVendorRequest;
import com.finx.noticemanagementservice.domain.enums.VendorType;
import com.finx.noticemanagementservice.service.VendorService;
import com.finx.noticemanagementservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> createVendor(
            @Valid @RequestBody CreateVendorRequest request) {
        NoticeVendorDTO vendor = vendorService.createVendor(request);
        return ResponseWrapper.created("Vendor created successfully", vendor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> getVendorById(@PathVariable Long id) {
        NoticeVendorDTO vendor = vendorService.getVendorById(id);
        return ResponseWrapper.ok("Vendor retrieved successfully", vendor);
    }

    @GetMapping("/code/{vendorCode}")
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> getVendorByCode(@PathVariable String vendorCode) {
        NoticeVendorDTO vendor = vendorService.getVendorByCode(vendorCode);
        return ResponseWrapper.ok("Vendor retrieved successfully", vendor);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<NoticeVendorDTO>>> getActiveVendors() {
        List<NoticeVendorDTO> vendors = vendorService.getActiveVendors();
        return ResponseWrapper.ok("Active vendors retrieved successfully", vendors);
    }

    @GetMapping("/type/{vendorType}")
    public ResponseEntity<CommonResponse<List<NoticeVendorDTO>>> getVendorsByType(
            @PathVariable VendorType vendorType) {
        List<NoticeVendorDTO> vendors = vendorService.getVendorsByType(vendorType);
        return ResponseWrapper.ok("Vendors retrieved successfully", vendors);
    }

    @GetMapping("/type/{vendorType}/active")
    public ResponseEntity<CommonResponse<List<NoticeVendorDTO>>> getActiveVendorsByType(
            @PathVariable VendorType vendorType) {
        List<NoticeVendorDTO> vendors = vendorService.getActiveVendorsByType(vendorType);
        return ResponseWrapper.ok("Active vendors retrieved successfully", vendors);
    }

    @GetMapping("/priority")
    public ResponseEntity<CommonResponse<List<NoticeVendorDTO>>> getActiveVendorsByPriority() {
        List<NoticeVendorDTO> vendors = vendorService.getActiveVendorsByPriority();
        return ResponseWrapper.ok("Vendors retrieved successfully", vendors);
    }

    @GetMapping("/pincode/{pincode}")
    public ResponseEntity<CommonResponse<List<NoticeVendorDTO>>> getVendorsServicingPincode(
            @PathVariable String pincode) {
        List<NoticeVendorDTO> vendors = vendorService.getVendorsServicingPincode(pincode);
        return ResponseWrapper.ok("Vendors retrieved successfully", vendors);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<NoticeVendorDTO>>> getAllVendors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeVendorDTO> vendors = vendorService.getAllVendors(pageable);
        return ResponseWrapper.ok("Vendors retrieved successfully", vendors);
    }

    @GetMapping("/status")
    public ResponseEntity<CommonResponse<Page<NoticeVendorDTO>>> getVendorsByActiveStatus(
            @RequestParam Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NoticeVendorDTO> vendors = vendorService.getVendorsByActiveStatus(isActive, pageable);
        return ResponseWrapper.ok("Vendors retrieved successfully", vendors);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVendorRequest request) {
        NoticeVendorDTO vendor = vendorService.updateVendor(id, request);
        return ResponseWrapper.ok("Vendor updated successfully", vendor);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> activateVendor(@PathVariable Long id) {
        NoticeVendorDTO vendor = vendorService.activateVendor(id);
        return ResponseWrapper.ok("Vendor activated successfully", vendor);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> deactivateVendor(@PathVariable Long id) {
        NoticeVendorDTO vendor = vendorService.deactivateVendor(id);
        return ResponseWrapper.ok("Vendor deactivated successfully", vendor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseWrapper.okMessage("Vendor deleted successfully");
    }

    @GetMapping("/select-best")
    public ResponseEntity<CommonResponse<NoticeVendorDTO>> selectBestVendor(
            @RequestParam VendorType vendorType,
            @RequestParam String pincode) {
        NoticeVendorDTO vendor = vendorService.selectBestVendor(vendorType, pincode);
        return ResponseWrapper.ok("Best vendor selected", vendor);
    }
}
