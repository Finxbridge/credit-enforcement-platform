package com.finx.configurationsservice.mapper;

import com.finx.configurationsservice.domain.dto.*;
import com.finx.configurationsservice.domain.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfigurationsMapper {

    // Organization mappings
    OrganizationDTO toDto(Organization organization);
    List<OrganizationDTO> toOrganizationDtoList(List<Organization> organizations);

    // Office mappings
    @Mapping(target = "parentOfficeName", ignore = true)
    @Mapping(target = "workCalendarName", ignore = true)
    OfficeDTO toDto(Office office);
    List<OfficeDTO> toOfficeDtoList(List<Office> offices);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Office toEntity(CreateOfficeRequest request);

    // WorkCalendar mappings
    WorkCalendarDTO toDto(WorkCalendar workCalendar);
    List<WorkCalendarDTO> toWorkCalendarDtoList(List<WorkCalendar> workCalendars);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WorkCalendar toEntity(CreateWorkCalendarRequest request);

    // Holiday mappings
    HolidayDTO toDto(Holiday holiday);
    List<HolidayDTO> toHolidayDtoList(List<Holiday> holidays);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Holiday toEntity(CreateHolidayRequest request);

    // Currency mappings
    CurrencyDTO toDto(Currency currency);
    List<CurrencyDTO> toCurrencyDtoList(List<Currency> currencies);

    // PasswordPolicy mappings
    PasswordPolicyDTO toDto(PasswordPolicy passwordPolicy);
    List<PasswordPolicyDTO> toPasswordPolicyDtoList(List<PasswordPolicy> policies);

    // ApprovalWorkflow mappings
    ApprovalWorkflowDTO toDto(ApprovalWorkflow approvalWorkflow);
    List<ApprovalWorkflowDTO> toApprovalWorkflowDtoList(List<ApprovalWorkflow> workflows);
}
