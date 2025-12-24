package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.config.CacheConstants;
import com.finx.configurationsservice.domain.dto.CreateOfficeRequest;
import com.finx.configurationsservice.domain.dto.OfficeDTO;
import com.finx.configurationsservice.domain.entity.Office;
import com.finx.configurationsservice.domain.enums.OfficeType;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.OfficeRepository;
import com.finx.configurationsservice.repository.WorkCalendarRepository;
import com.finx.configurationsservice.service.OfficeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final WorkCalendarRepository workCalendarRepository;
    private final ConfigurationsMapper mapper;

    private static final AtomicLong OFFICE_SEQUENCE = new AtomicLong(System.currentTimeMillis() % 100000);

    /**
     * Generates a unique office code in format: OFF-YYYYMMDD-XXXXX
     */
    private String generateOfficeCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%05d", OFFICE_SEQUENCE.incrementAndGet() % 100000);
        String officeCode = "OFF-" + datePart + "-" + sequencePart;

        // Ensure uniqueness
        while (officeRepository.existsByOfficeCode(officeCode)) {
            sequencePart = String.format("%05d", OFFICE_SEQUENCE.incrementAndGet() % 100000);
            officeCode = "OFF-" + datePart + "-" + sequencePart;
        }
        return officeCode;
    }

    @Override
    @CacheEvict(value = {CacheConstants.OFFICE_CACHE, CacheConstants.OFFICE_LIST_CACHE}, allEntries = true)
    public OfficeDTO createOffice(CreateOfficeRequest request) {
        // Auto-generate office code
        String officeCode = generateOfficeCode();
        log.info("Auto-generated office code: {}", officeCode);

        Office office = mapper.toEntity(request);
        office.setOfficeCode(officeCode);
        office.setIsActive(true);

        Office savedOffice = officeRepository.save(office);
        log.info("Office created with ID: {}", savedOffice.getId());

        return enrichWithDetails(mapper.toDto(savedOffice));
    }

    @Override
    @Cacheable(value = CacheConstants.OFFICE_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public OfficeDTO getOfficeById(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
        return enrichWithDetails(mapper.toDto(office));
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeDTO getOfficeByCode(String officeCode) {
        Office office = officeRepository.findByOfficeCode(officeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Office", officeCode));
        return enrichWithDetails(mapper.toDto(office));
    }

    @Override
    @Cacheable(value = CacheConstants.OFFICE_LIST_CACHE, key = "'active'")
    @Transactional(readOnly = true)
    public List<OfficeDTO> getActiveOffices() {
        return officeRepository.findByIsActiveTrue().stream()
                .map(mapper::toDto)
                .map(this::enrichWithDetails)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeDTO> getOfficesByType(OfficeType type) {
        return officeRepository.findActiveOfficesByType(type).stream()
                .map(mapper::toDto)
                .map(this::enrichWithDetails)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeDTO> getOfficesByState(String state) {
        return officeRepository.findActiveOfficesByState(state).stream()
                .map(mapper::toDto)
                .map(this::enrichWithDetails)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeDTO> getChildOffices(Long parentOfficeId) {
        return officeRepository.findByParentOfficeId(parentOfficeId).stream()
                .map(mapper::toDto)
                .map(this::enrichWithDetails)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfficeDTO> getAllOffices(Pageable pageable) {
        return officeRepository.findAll(pageable)
                .map(mapper::toDto)
                .map(this::enrichWithDetails);
    }

    @Override
    @CacheEvict(value = {CacheConstants.OFFICE_CACHE, CacheConstants.OFFICE_LIST_CACHE}, allEntries = true)
    public OfficeDTO updateOffice(Long id, CreateOfficeRequest request) {
        log.info("Updating office ID: {}", id);

        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));

        office.setOfficeName(request.getOfficeName());
        office.setOfficeType(request.getOfficeType());
        office.setParentOfficeId(request.getParentOfficeId());
        office.setAddress(request.getAddress());
        office.setCity(request.getCity());
        office.setState(request.getState());
        office.setPincode(request.getPincode());
        office.setLatitude(request.getLatitude());
        office.setLongitude(request.getLongitude());
        office.setPhone(request.getPhone());
        office.setEmail(request.getEmail());
        office.setOpeningDate(request.getOpeningDate());
        office.setWorkCalendarId(request.getWorkCalendarId());

        Office savedOffice = officeRepository.save(office);
        return enrichWithDetails(mapper.toDto(savedOffice));
    }

    @Override
    @CacheEvict(value = {CacheConstants.OFFICE_CACHE, CacheConstants.OFFICE_LIST_CACHE}, allEntries = true)
    public OfficeDTO activateOffice(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
        office.setIsActive(true);
        return enrichWithDetails(mapper.toDto(officeRepository.save(office)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.OFFICE_CACHE, CacheConstants.OFFICE_LIST_CACHE}, allEntries = true)
    public OfficeDTO deactivateOffice(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
        office.setIsActive(false);
        return enrichWithDetails(mapper.toDto(officeRepository.save(office)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.OFFICE_CACHE, CacheConstants.OFFICE_LIST_CACHE}, allEntries = true)
    public void deleteOffice(Long id) {
        if (!officeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Office", id);
        }
        officeRepository.deleteById(id);
        log.info("Office deleted: {}", id);
    }

    private OfficeDTO enrichWithDetails(OfficeDTO dto) {
        if (dto.getParentOfficeId() != null) {
            officeRepository.findById(dto.getParentOfficeId())
                    .ifPresent(parent -> dto.setParentOfficeName(parent.getOfficeName()));
        }
        if (dto.getWorkCalendarId() != null) {
            workCalendarRepository.findById(dto.getWorkCalendarId())
                    .ifPresent(calendar -> dto.setWorkCalendarName(calendar.getCalendarName()));
        }
        return dto;
    }
}
