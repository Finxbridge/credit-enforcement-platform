package com.finx.noticemanagementservice.service;

import com.finx.noticemanagementservice.domain.dto.CreateVendorRequest;
import com.finx.noticemanagementservice.domain.dto.NoticeVendorDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateVendorRequest;
import com.finx.noticemanagementservice.domain.enums.VendorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VendorService {

    NoticeVendorDTO createVendor(CreateVendorRequest request);

    NoticeVendorDTO getVendorById(Long id);

    NoticeVendorDTO getVendorByCode(String vendorCode);

    List<NoticeVendorDTO> getActiveVendors();

    List<NoticeVendorDTO> getVendorsByType(VendorType vendorType);

    List<NoticeVendorDTO> getActiveVendorsByType(VendorType vendorType);

    List<NoticeVendorDTO> getActiveVendorsByPriority();

    List<NoticeVendorDTO> getVendorsServicingPincode(String pincode);

    Page<NoticeVendorDTO> getAllVendors(Pageable pageable);

    Page<NoticeVendorDTO> getVendorsByActiveStatus(Boolean isActive, Pageable pageable);

    NoticeVendorDTO updateVendor(Long id, UpdateVendorRequest request);

    NoticeVendorDTO activateVendor(Long id);

    NoticeVendorDTO deactivateVendor(Long id);

    void deleteVendor(Long id);

    NoticeVendorDTO selectBestVendor(VendorType vendorType, String pincode);
}
