package com.finx.masterdataservice.service;

import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface MasterDataService {
    List<MasterDataDTO> getMasterDataByType(String type);

    Map<String, Object> bulkUploadMasterData(String type, MultipartFile file);

    Map<String, Object> bulkUploadMasterDataV2(MultipartFile file);

    void deleteMasterData(Long id);

    MasterDataDTO updateMasterData(Long id, MasterDataDTO masterDataDTO);

    void deleteMasterDataByType(String type);
}
