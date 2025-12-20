package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.CreateOfficeRequest;
import com.finx.configurationsservice.domain.dto.OfficeDTO;
import com.finx.configurationsservice.domain.enums.OfficeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OfficeService {

    OfficeDTO createOffice(CreateOfficeRequest request);

    OfficeDTO getOfficeById(Long id);

    OfficeDTO getOfficeByCode(String officeCode);

    List<OfficeDTO> getActiveOffices();

    List<OfficeDTO> getOfficesByType(OfficeType type);

    List<OfficeDTO> getOfficesByState(String state);

    List<OfficeDTO> getChildOffices(Long parentOfficeId);

    Page<OfficeDTO> getAllOffices(Pageable pageable);

    OfficeDTO updateOffice(Long id, CreateOfficeRequest request);

    OfficeDTO activateOffice(Long id);

    OfficeDTO deactivateOffice(Long id);

    void deleteOffice(Long id);
}
