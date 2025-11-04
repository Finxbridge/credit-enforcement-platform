package com.finx.masterdataservice.controller;

import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import com.finx.masterdataservice.service.MasterDataService;
import com.finx.masterdataservice.domain.dto.CommonResponse;
import com.finx.masterdataservice.util.ResponseWrapper;
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

    @PostMapping("/bulk-upload-v2")
    public ResponseEntity<CommonResponse<Map<String, Object>>> bulkUploadMasterDataV2(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = masterDataService.bulkUploadMasterDataV2(file);
        return ResponseWrapper.ok("Master data bulk upload initiated.", result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteMasterData(@PathVariable Long id) {
        masterDataService.deleteMasterData(id);
        return ResponseWrapper.ok("Master data deleted successfully.", null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<MasterDataDTO>> updateMasterData(@PathVariable Long id, @RequestBody MasterDataDTO masterDataDTO) {
        MasterDataDTO updatedMasterData = masterDataService.updateMasterData(id, masterDataDTO);
        return ResponseWrapper.ok("Master data updated successfully.", updatedMasterData);
    }

    @DeleteMapping("/type/{type}")
    public ResponseEntity<CommonResponse<Void>> deleteMasterDataByType(@PathVariable String type) {
        masterDataService.deleteMasterDataByType(type);
        return ResponseWrapper.ok("Master data for type '" + type + "' deleted successfully.", null);
    }
}
