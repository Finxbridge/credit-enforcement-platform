package com.finx.masterdataservice.controller;

import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import com.finx.masterdataservice.service.MasterDataService;
import com.finx.common.domain.dto.CommonResponse;
import com.finx.common.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<MasterDataDTO>>> getMasterDataByType(@RequestParam String type) {
        List<MasterDataDTO> masterData = masterDataService.getMasterDataByType(type);
        return ResponseWrapper.ok("Master data retrieved successfully.", masterData);
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<CommonResponse<Map<String, Object>>> bulkUploadMasterData(
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = masterDataService.bulkUploadMasterData(type, file);
        return ResponseWrapper.ok("Master data bulk upload initiated.", result);
    }
}
