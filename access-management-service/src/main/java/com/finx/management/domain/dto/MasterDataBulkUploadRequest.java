package com.finx.management.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MasterDataBulkUploadRequest {
    private String type;
    private MultipartFile file;
}
