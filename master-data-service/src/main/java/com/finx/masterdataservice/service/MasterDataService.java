package com.finx.masterdataservice.service;

import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface MasterDataService {
    List<MasterDataDTO> getMasterDataByType(String type);

    Map<String, Object> bulkUploadMasterData(String type, MultipartFile file);
}
