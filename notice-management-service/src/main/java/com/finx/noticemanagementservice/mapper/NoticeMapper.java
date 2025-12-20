package com.finx.noticemanagementservice.mapper;

import com.finx.noticemanagementservice.domain.dto.*;
import com.finx.noticemanagementservice.domain.entity.Notice;
import com.finx.noticemanagementservice.domain.entity.NoticeVendor;
import com.finx.noticemanagementservice.domain.entity.ProofOfDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    // Notice mappings
    @Mapping(target = "vendorName", ignore = true)
    NoticeDTO toDto(Notice notice);

    List<NoticeDTO> toDtoList(List<Notice> notices);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "noticeNumber", ignore = true)
    @Mapping(target = "noticeStatus", ignore = true)
    @Mapping(target = "generatedContent", source = "renderedContent")
    @Mapping(target = "pdfUrl", ignore = true)
    @Mapping(target = "pageCount", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    @Mapping(target = "generatedBy", ignore = true)
    @Mapping(target = "vendorId", ignore = true)
    @Mapping(target = "dispatchedAt", ignore = true)
    @Mapping(target = "dispatchedBy", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "carrierName", ignore = true)
    @Mapping(target = "expectedDeliveryAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "podId", ignore = true)
    @Mapping(target = "rtoAt", ignore = true)
    @Mapping(target = "rtoReason", ignore = true)
    @Mapping(target = "dispatchSlaBreach", ignore = true)
    @Mapping(target = "deliverySlaBreach", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    // DMS Document fields are mapped automatically by name
    Notice toEntity(CreateNoticeRequest request);

    // NoticeVendor mappings
    NoticeVendorDTO toVendorDto(NoticeVendor vendor);

    List<NoticeVendorDTO> toVendorDtoList(List<NoticeVendor> vendors);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "deliveryRate", ignore = true)
    @Mapping(target = "rtoRate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    NoticeVendor toVendorEntity(CreateVendorRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendorCode", ignore = true)
    @Mapping(target = "deliveryRate", ignore = true)
    @Mapping(target = "rtoRate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVendorFromRequest(UpdateVendorRequest request, @MappingTarget NoticeVendor vendor);

    // ProofOfDelivery mappings
    @Mapping(target = "noticeNumber", ignore = true)
    @Mapping(target = "vendorName", ignore = true)
    ProofOfDeliveryDTO toPodDto(ProofOfDelivery pod);

    List<ProofOfDeliveryDTO> toPodDtoList(List<ProofOfDelivery> pods);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podNumber", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verificationRemarks", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    ProofOfDelivery toPodEntity(UploadPodRequest request);
}
