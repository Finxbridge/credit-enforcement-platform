package com.finx.masterdataservice.controller;

import com.finx.masterdataservice.domain.dto.CreateMasterDataRequest;
import com.finx.masterdataservice.domain.dto.MasterDataDTO;
import com.finx.masterdataservice.domain.dto.MasterDataResponse;
import com.finx.masterdataservice.service.MasterDataService;
import com.finx.masterdataservice.domain.dto.CommonResponse;
import com.finx.masterdataservice.util.CsvTemplateGenerator;
import com.finx.masterdataservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;
    private final CsvTemplateGenerator csvTemplateGenerator;

    @GetMapping
    public ResponseEntity<CommonResponse<List<MasterDataDTO>>> getMasterDataByType(@RequestParam String type) {
        List<MasterDataDTO> masterData = masterDataService.getMasterDataByType(type);
        return ResponseWrapper.ok("Master data retrieved successfully.", masterData);
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<MasterDataResponse>> getAllMasterData() {
        log.info("GET /master-data/all - Fetching all master data with category counts");
        MasterDataResponse response = masterDataService.getAllMasterData();
        return ResponseWrapper.ok("All master data retrieved successfully.", response);
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<MasterDataDTO>> createMasterData(
            @Valid @RequestBody CreateMasterDataRequest request) {
        log.info("POST /master-data/create - Creating master data with categoryType: {}, code: {}",
                request.getCategoryType(), request.getCode());
        MasterDataDTO createdMasterData = masterDataService.createMasterData(request);
        return ResponseWrapper.created("Master data created successfully.", createdMasterData);
    }

    @PostMapping("/bulk-upload-by-type")
    public ResponseEntity<CommonResponse<Map<String, Object>>> bulkUploadMasterData(
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = masterDataService.bulkUploadMasterData(type, file);
        return ResponseWrapper.ok("Master data bulk upload initiated.", result);
    }

    @PostMapping("/bulk-upload")
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
    public ResponseEntity<CommonResponse<MasterDataDTO>> updateMasterData(@PathVariable Long id,
            @RequestBody MasterDataDTO masterDataDTO) {
        MasterDataDTO updatedMasterData = masterDataService.updateMasterData(id, masterDataDTO);
        return ResponseWrapper.ok("Master data updated successfully.", updatedMasterData);
    }

    @DeleteMapping("/type/{type}")
    public ResponseEntity<CommonResponse<Void>> deleteMasterDataByType(@PathVariable String type) {
        masterDataService.deleteMasterDataByType(type);
        return ResponseWrapper.ok("Master data for type '" + type + "' deleted successfully.", null);
    }

    @GetMapping("/upload/template")
    public ResponseEntity<byte[]> downloadUploadTemplate(
            @RequestParam(defaultValue = "false") boolean includeSample) {
        log.info("GET /master-data/upload/template - Downloading V1 template (includeSample: {})", includeSample);

        byte[] csvData = csvTemplateGenerator.generateMasterDataTemplateV1(includeSample);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "master_data_upload_template.csv");

        return ResponseEntity.ok().headers(headers).body(csvData);
    }

    @GetMapping("/upload/template-v2")
    public ResponseEntity<byte[]> downloadUploadTemplateV2(
            @RequestParam(defaultValue = "false") boolean includeSample) {
        log.info("GET /master-data/upload/template-v2 - Downloading V2 template (includeSample: {})", includeSample);

        byte[] csvData = csvTemplateGenerator.generateMasterDataTemplateV2(includeSample);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "master_data_upload_template_v2.csv");

        return ResponseEntity.ok().headers(headers).body(csvData);
    }
}
