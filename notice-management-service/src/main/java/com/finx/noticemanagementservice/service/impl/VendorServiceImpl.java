package com.finx.noticemanagementservice.service.impl;

import com.finx.noticemanagementservice.config.CacheConstants;
import com.finx.noticemanagementservice.domain.dto.CreateVendorRequest;
import com.finx.noticemanagementservice.domain.dto.NoticeVendorDTO;
import com.finx.noticemanagementservice.domain.dto.UpdateVendorRequest;
import com.finx.noticemanagementservice.domain.entity.NoticeVendor;
import com.finx.noticemanagementservice.domain.enums.VendorType;
import com.finx.noticemanagementservice.exception.BusinessException;
import com.finx.noticemanagementservice.exception.ResourceNotFoundException;
import com.finx.noticemanagementservice.mapper.NoticeMapper;
import com.finx.noticemanagementservice.repository.NoticeVendorRepository;
import com.finx.noticemanagementservice.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VendorServiceImpl implements VendorService {

    private final NoticeVendorRepository vendorRepository;
    private final NoticeMapper noticeMapper;

    @Override
    @CacheEvict(value = {CacheConstants.VENDOR_CACHE, CacheConstants.VENDOR_LIST_CACHE}, allEntries = true)
    public NoticeVendorDTO createVendor(CreateVendorRequest request) {
        log.info("Creating vendor: {}", request.getVendorCode());

        if (vendorRepository.existsByVendorCode(request.getVendorCode())) {
            throw new BusinessException("Vendor code already exists: " + request.getVendorCode());
        }

        NoticeVendor vendor = noticeMapper.toVendorEntity(request);
        vendor.setIsActive(true);
        vendor.setCreatedBy(request.getCreatedBy());

        NoticeVendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendor created with ID: {}", savedVendor.getId());

        return noticeMapper.toVendorDto(savedVendor);
    }

    @Override
    @Cacheable(value = CacheConstants.VENDOR_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public NoticeVendorDTO getVendorById(Long id) {
        NoticeVendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));
        return noticeMapper.toVendorDto(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeVendorDTO getVendorByCode(String vendorCode) {
        NoticeVendor vendor = vendorRepository.findByVendorCode(vendorCode)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorCode));
        return noticeMapper.toVendorDto(vendor);
    }

    @Override
    @Cacheable(value = CacheConstants.VENDOR_LIST_CACHE, key = "'active'")
    @Transactional(readOnly = true)
    public List<NoticeVendorDTO> getActiveVendors() {
        return noticeMapper.toVendorDtoList(vendorRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeVendorDTO> getVendorsByType(VendorType vendorType) {
        return noticeMapper.toVendorDtoList(vendorRepository.findByVendorType(vendorType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeVendorDTO> getActiveVendorsByType(VendorType vendorType) {
        return noticeMapper.toVendorDtoList(vendorRepository.findByVendorTypeAndIsActiveTrue(vendorType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeVendorDTO> getActiveVendorsByPriority() {
        return noticeMapper.toVendorDtoList(vendorRepository.findActiveVendorsByPriority());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeVendorDTO> getVendorsServicingPincode(String pincode) {
        return noticeMapper.toVendorDtoList(vendorRepository.findVendorsServicingPincode(pincode));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeVendorDTO> getAllVendors(Pageable pageable) {
        return vendorRepository.findAll(pageable)
                .map(noticeMapper::toVendorDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeVendorDTO> getVendorsByActiveStatus(Boolean isActive, Pageable pageable) {
        return vendorRepository.findByIsActive(isActive, pageable)
                .map(noticeMapper::toVendorDto);
    }

    @Override
    @CacheEvict(value = {CacheConstants.VENDOR_CACHE, CacheConstants.VENDOR_LIST_CACHE}, allEntries = true)
    public NoticeVendorDTO updateVendor(Long id, UpdateVendorRequest request) {
        log.info("Updating vendor ID: {}", id);

        NoticeVendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));

        noticeMapper.updateVendorFromRequest(request, vendor);
        vendor.setUpdatedBy(request.getUpdatedBy());

        NoticeVendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendor updated: {}", savedVendor.getId());

        return noticeMapper.toVendorDto(savedVendor);
    }

    @Override
    @CacheEvict(value = {CacheConstants.VENDOR_CACHE, CacheConstants.VENDOR_LIST_CACHE}, allEntries = true)
    public NoticeVendorDTO activateVendor(Long id) {
        log.info("Activating vendor ID: {}", id);

        NoticeVendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));

        vendor.setIsActive(true);
        NoticeVendor savedVendor = vendorRepository.save(vendor);

        return noticeMapper.toVendorDto(savedVendor);
    }

    @Override
    @CacheEvict(value = {CacheConstants.VENDOR_CACHE, CacheConstants.VENDOR_LIST_CACHE}, allEntries = true)
    public NoticeVendorDTO deactivateVendor(Long id) {
        log.info("Deactivating vendor ID: {}", id);

        NoticeVendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));

        vendor.setIsActive(false);
        NoticeVendor savedVendor = vendorRepository.save(vendor);

        return noticeMapper.toVendorDto(savedVendor);
    }

    @Override
    @CacheEvict(value = {CacheConstants.VENDOR_CACHE, CacheConstants.VENDOR_LIST_CACHE}, allEntries = true)
    public void deleteVendor(Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor", id);
        }
        vendorRepository.deleteById(id);
        log.info("Vendor deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeVendorDTO selectBestVendor(VendorType vendorType, String pincode) {
        // First try to find vendors that service the pincode
        List<NoticeVendor> vendors = vendorRepository.findVendorsServicingPincode(pincode);

        // Filter by vendor type and active status
        vendors = vendors.stream()
                .filter(v -> v.getVendorType() == vendorType && Boolean.TRUE.equals(v.getIsActive()))
                .toList();

        if (vendors.isEmpty()) {
            // Fall back to active vendors by type ordered by priority
            vendors = vendorRepository.findActiveVendorsByTypeAndPriority(vendorType);
        }

        if (vendors.isEmpty()) {
            throw new ResourceNotFoundException("No active vendor found for type: " + vendorType);
        }

        // Return the first (highest priority) vendor
        return noticeMapper.toVendorDto(vendors.get(0));
    }
}
