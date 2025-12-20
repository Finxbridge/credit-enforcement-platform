package com.finx.agencymanagement.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.agencymanagement.domain.dto.AgencyDTO;
import com.finx.agencymanagement.domain.dto.AgencyUserDTO;
import com.finx.agencymanagement.domain.dto.CreateAgencyRequest;
import com.finx.agencymanagement.domain.dto.CreateAgencyUserRequest;
import com.finx.agencymanagement.domain.entity.Agency;
import com.finx.agencymanagement.domain.entity.AgencyUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Agency Mapper using MapStruct
 * Maps between Entity and DTO with field name translations
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AgencyMapper {

    // ==========================================
    // Agency Entity <-> AgencyDTO mappings
    // ==========================================

    @Mapping(source = "addressLine1", target = "address")
    @Mapping(source = "bankIfsc", target = "ifscCode")
    @Mapping(source = "commissionPercentage", target = "commissionRate")
    @Mapping(source = "maximumCases", target = "maxCaseLimit")
    @Mapping(source = "activeCasesCount", target = "currentCaseCount")
    @Mapping(source = "approvedAt", target = "approvalDate")
    @Mapping(source = "approvalNotes", target = "notes")
    @Mapping(source = "kycDocuments", target = "kycDocuments", qualifiedByName = "jsonToKycDocuments")
    @Mapping(source = "serviceAreas", target = "serviceAreas", qualifiedByName = "jsonToStringList")
    @Mapping(source = "servicePincodes", target = "servicePincodes", qualifiedByName = "jsonToStringList")
    AgencyDTO toDto(Agency agency);

    ObjectMapper objectMapper = new ObjectMapper();

    @Named("jsonToKycDocuments")
    default List<Map<String, Object>> jsonToKycDocuments(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Named("jsonToStringList")
    default List<String> jsonToStringList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Named("kycDocumentsToJson")
    default String kycDocumentsToJson(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    @Named("stringListToJson")
    default String stringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    // ==========================================
    // CreateAgencyRequest -> Agency Entity
    // ==========================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agencyCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "address", target = "addressLine1")
    @Mapping(target = "addressLine2", ignore = true)
    @Mapping(target = "country", constant = "India")
    @Mapping(source = "ifscCode", target = "bankIfsc")
    @Mapping(target = "bankBranch", ignore = true)
    @Mapping(source = "commissionRate", target = "commissionPercentage")
    @Mapping(target = "minimumCases", ignore = true)
    @Mapping(source = "maxCaseLimit", target = "maximumCases")
    @Mapping(target = "registrationNumber", ignore = true)
    // Optional fields - mapped directly (JSON strings stored in JSONB columns)
    @Mapping(source = "kycDocuments", target = "kycDocuments")
    @Mapping(source = "serviceAreas", target = "serviceAreas")
    @Mapping(source = "servicePincodes", target = "servicePincodes")
    @Mapping(target = "totalCasesAllocated", ignore = true)
    @Mapping(target = "totalCasesResolved", ignore = true)
    @Mapping(target = "resolutionRate", ignore = true)
    @Mapping(target = "ptpSuccessRate", ignore = true)
    @Mapping(target = "activeCasesCount", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "submittedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(source = "notes", target = "approvalNotes")
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectedBy", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "alternatePhone", ignore = true)
    Agency toEntity(CreateAgencyRequest request);

    // ==========================================
    // AgencyDTO -> Agency Entity (for updates)
    // ==========================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agencyCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "address", target = "addressLine1")
    @Mapping(target = "addressLine2", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(source = "ifscCode", target = "bankIfsc")
    @Mapping(target = "bankBranch", ignore = true)
    @Mapping(source = "commissionRate", target = "commissionPercentage")
    @Mapping(target = "minimumCases", ignore = true)
    @Mapping(source = "maxCaseLimit", target = "maximumCases")
    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(source = "notes", target = "approvalNotes")
    // Map JSONB fields using qualifiedByName
    @Mapping(source = "kycDocuments", target = "kycDocuments", qualifiedByName = "kycDocumentsToJson")
    @Mapping(source = "serviceAreas", target = "serviceAreas", qualifiedByName = "stringListToJson")
    @Mapping(source = "servicePincodes", target = "servicePincodes", qualifiedByName = "stringListToJson")
    @Mapping(target = "totalCasesAllocated", ignore = true)
    @Mapping(target = "totalCasesResolved", ignore = true)
    @Mapping(target = "resolutionRate", ignore = true)
    @Mapping(target = "ptpSuccessRate", ignore = true)
    @Mapping(target = "activeCasesCount", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "submittedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "alternatePhone", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AgencyDTO dto, @MappingTarget Agency agency);

    // ==========================================
    // AgencyUser Entity <-> AgencyUserDTO mappings
    // ==========================================

    @Mapping(source = "userCode", target = "employeeCode")
    @Mapping(source = "mobileNumber", target = "phoneNumber")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "department", ignore = true)
    AgencyUserDTO toDto(AgencyUser agencyUser);

    // ==========================================
    // CreateAgencyUserRequest -> AgencyUser Entity
    // ==========================================

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "employeeCode", target = "userCode")
    @Mapping(source = "phoneNumber", target = "mobileNumber")
    @Mapping(target = "alternateMobile", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "pincode", ignore = true)
    @Mapping(target = "assignedPincodes", ignore = true)
    @Mapping(target = "assignedGeographies", ignore = true)
    @Mapping(target = "maxCaseCapacity", ignore = true)
    @Mapping(target = "currentCaseCount", ignore = true)
    @Mapping(target = "totalCasesHandled", ignore = true)
    @Mapping(target = "casesResolved", ignore = true)
    @Mapping(target = "ptpCaptured", ignore = true)
    @Mapping(target = "ptpKept", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "deactivatedAt", ignore = true)
    @Mapping(target = "deactivatedBy", ignore = true)
    @Mapping(target = "deactivationReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    AgencyUser toEntity(CreateAgencyUserRequest request);
}
