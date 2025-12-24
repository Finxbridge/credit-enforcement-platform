package com.finx.collectionsservice.service.impl;

import com.finx.collectionsservice.domain.dto.OTSCaseSearchDTO;
import com.finx.collectionsservice.domain.entity.Case;
import com.finx.collectionsservice.domain.entity.Customer;
import com.finx.collectionsservice.domain.entity.LoanDetails;
import com.finx.collectionsservice.exception.ResourceNotFoundException;
import com.finx.collectionsservice.repository.CaseRepository;
import com.finx.collectionsservice.service.CaseSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for searching cases for OTS creation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaseSearchServiceImpl implements CaseSearchService {

    private final CaseRepository caseRepository;

    @Override
    public Page<OTSCaseSearchDTO> searchCases(String query, Pageable pageable) {
        log.info("Searching cases with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        String searchTerm = query.trim();
        Page<Case> cases = caseRepository.searchCases(searchTerm, pageable);

        log.info("Found {} cases matching query: {}", cases.getTotalElements(), query);
        return cases.map(this::mapToDTO);
    }

    @Override
    public OTSCaseSearchDTO getCaseDetails(Long caseId) {
        log.info("Fetching case details for OTS: {}", caseId);

        Case caseEntity = caseRepository.findByIdWithDetails(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case", caseId));

        return mapToDTO(caseEntity);
    }

    /**
     * Map Case entity to OTSCaseSearchDTO
     */
    private OTSCaseSearchDTO mapToDTO(Case caseEntity) {
        LoanDetails loan = caseEntity.getLoan();
        Customer customer = loan.getPrimaryCustomer();

        return OTSCaseSearchDTO.builder()
                // Case Details
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .caseStatus(caseEntity.getCaseStatus())
                .casePriority(caseEntity.getCasePriority())
                // Customer Details
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .mobileNumber(customer.getMobileNumber())
                .emailAddress(customer.getEmailAddress())
                .address(customer.getAddressLine1())
                .city(customer.getCity())
                .state(customer.getState())
                .pincode(customer.getPincode())
                // Loan Details
                .loanId(loan.getId())
                .loanAccountNumber(loan.getLoanAccountNumber())
                .productType(loan.getProductType())
                .bankCode(loan.getBankCode())
                // Outstanding Details
                .principalAmount(loan.getPrincipalAmount())
                .interestAmount(loan.getInterestAmount())
                .penaltyAmount(loan.getPenaltyAmount())
                .totalOutstanding(loan.getTotalOutstanding())
                .emiAmount(loan.getEmiAmount())
                // Risk Details
                .dpd(loan.getDpd())
                .bucket(loan.getBucket())
                .dueDate(loan.getDueDate())
                // Allocation Details
                .allocatedToUserId(caseEntity.getAllocatedToUserId())
                .allocatedToAgencyId(caseEntity.getAllocatedToAgencyId())
                .collectionCycle(caseEntity.getCollectionCycle())
                .build();
    }
}
