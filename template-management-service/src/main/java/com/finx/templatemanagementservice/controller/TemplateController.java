package com.finx.templatemanagementservice.controller;

import com.finx.templatemanagementservice.domain.dto.*;
import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.service.TemplateService;
import com.finx.templatemanagementservice.service.VariableDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for Template Management
 *
 * Simplified API Flow:
 * 1. Frontend selects channel (SMS, WhatsApp, Email, Notice)
 * 2. Frontend shows available variables from GET /templates/variables/available
 * 3. User creates template content with {{variableName}} placeholders
 * 4. For non-SMS channels, user can attach a document
 * 5. Frontend sends simplified request to create template
 */
@Slf4j
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@Tag(name = "Template Management", description = "APIs for managing communication templates")
public class TemplateController {

    private final TemplateService templateService;
    private final VariableDefinitionService variableDefinitionService;

    // ==================== 1. Create Template (with or without document) ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create template",
               description = "Create a new template. Supports optional document attachment for WHATSAPP, EMAIL, NOTICE channels. " +
                           "Template content should contain variables like {{customerName}}, {{loanAccount}}")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> createTemplate(
            @RequestPart("template") @Valid SimpleCreateTemplateRequest request,
            @RequestPart(value = "document", required = false) MultipartFile document) {

        log.info("POST /api/v1/templates - Creating template: {} for channel: {}",
                request.getTemplateName(), request.getChannel());

        TemplateDetailDTO template = templateService.createTemplateSimplified(request, document);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("Template created successfully", template));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create template (JSON only)",
               description = "Create a new template without document attachment. " +
                           "Template content should contain variables like {{customerName}}, {{loanAccount}}")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> createTemplateJson(
            @Valid @RequestBody SimpleCreateTemplateRequest request) {

        log.info("POST /api/v1/templates - Creating template (JSON): {} for channel: {}",
                request.getTemplateName(), request.getChannel());

        TemplateDetailDTO template = templateService.createTemplateSimplified(request, null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("Template created successfully", template));
    }

    // ==================== 2. Get Available Variables ====================

    @GetMapping("/variables/available")
    @Operation(summary = "Get available variables",
               description = "Get list of available variables that can be used in templates. " +
                           "Use variableKey in template content like {{customer_name}}, {{loan_account_number}}")
    public ResponseEntity<CommonResponse<List<AvailableVariableDTO>>> getAvailableVariables() {
        log.info("GET /api/v1/templates/variables/available - Fetching available variables");
        List<VariableDefinitionDTO> variables = variableDefinitionService.getActiveVariables();

        // Map to simplified DTO with only essential fields
        List<AvailableVariableDTO> availableVariables = variables.stream()
                .map(v -> AvailableVariableDTO.builder()
                        .variableKey(v.getVariableKey())
                        .exampleValue(v.getExampleValue())
                        .isActive(v.getIsActive())
                        .build())
                .toList();

        return ResponseEntity.ok(CommonResponse.success("Available variables retrieved successfully", availableVariables));
    }

    // ==================== 3. Get Template by ID ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get template", description = "Get template details by ID")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> getTemplate(@PathVariable Long id) {
        log.info("GET /api/v1/templates/{} - Fetching template", id);
        TemplateDetailDTO template = templateService.getTemplate(id);
        return ResponseEntity.ok(CommonResponse.success("Template retrieved successfully", template));
    }

    // ==================== 4. Get All Templates ====================

    @GetMapping
    @Operation(summary = "Get all templates", description = "Get list of all active templates")
    public ResponseEntity<CommonResponse<List<TemplateDTO>>> getAllTemplates() {
        log.info("GET /api/v1/templates - Fetching all templates");
        List<TemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(CommonResponse.success("Templates retrieved successfully", templates));
    }

    // ==================== 5. Get Templates by Channel ====================

    @GetMapping("/channel/{channel}")
    @Operation(summary = "Get templates by channel",
               description = "Get templates filtered by communication channel (SMS, WHATSAPP, EMAIL, NOTICE, IVR)")
    public ResponseEntity<CommonResponse<List<TemplateDTO>>> getTemplatesByChannel(
            @PathVariable ChannelType channel) {
        log.info("GET /api/v1/templates/channel/{} - Fetching templates by channel", channel);
        List<TemplateDTO> templates = templateService.getTemplatesByChannel(channel);
        return ResponseEntity.ok(CommonResponse.success("Templates retrieved successfully", templates));
    }

    // ==================== 6. Resolve Template Variables ====================

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve template variables",
               description = "Resolve template variables and render content for a specific case. " +
                           "Also processes document placeholders if document is attached.")
    public ResponseEntity<CommonResponse<TemplateResolveResponse>> resolveTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateResolveRequest request) {
        log.info("POST /api/v1/templates/{}/resolve - Resolving template for case: {}", id, request.getCaseId());
        TemplateResolveResponse response = templateService.resolveTemplateWithDocument(id, request);
        return ResponseEntity.ok(CommonResponse.success("Template resolved successfully", response));
    }

    // ==================== 7. Update Template ====================

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update template",
               description = "Update template. Can optionally update/add document attachment.")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> updateTemplate(
            @PathVariable Long id,
            @RequestPart("template") @Valid SimpleCreateTemplateRequest request,
            @RequestPart(value = "document", required = false) MultipartFile document) {
        log.info("PUT /api/v1/templates/{} - Updating template", id);
        TemplateDetailDTO template = templateService.updateTemplateSimplified(id, request, document);
        return ResponseEntity.ok(CommonResponse.success("Template updated successfully", template));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update template (JSON only)",
               description = "Update template without changing document attachment.")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> updateTemplateJson(
            @PathVariable Long id,
            @Valid @RequestBody SimpleCreateTemplateRequest request) {
        log.info("PUT /api/v1/templates/{} - Updating template (JSON)", id);
        TemplateDetailDTO template = templateService.updateTemplateSimplified(id, request, null);
        return ResponseEntity.ok(CommonResponse.success("Template updated successfully", template));
    }

    // ==================== 8. Delete Template ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template", description = "Soft delete a template")
    public ResponseEntity<CommonResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("DELETE /api/v1/templates/{} - Deleting template", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(CommonResponse.successMessage("Template deleted successfully"));
    }

    // ==================== 9. Dropdown API by Channel (for Strategy Engine) ====================

    @GetMapping("/dropdown/{channel}")
    @Operation(summary = "Get templates for dropdown by channel",
               description = "Get lightweight list of templates filtered by channel for dropdown selection.")
    public ResponseEntity<CommonResponse<List<TemplateDropdownDTO>>> getTemplatesForDropdownByChannel(
            @PathVariable ChannelType channel) {
        log.info("GET /api/v1/templates/dropdown/{} - Fetching templates for dropdown by channel", channel);
        List<TemplateDTO> templates = templateService.getTemplatesByChannel(channel);
        List<TemplateDropdownDTO> dropdownList = templates.stream()
                .map(t -> TemplateDropdownDTO.builder()
                        .id(t.getId())
                        .templateName(t.getTemplateName())
                        .templateCode(t.getTemplateCode())
                        .channel(t.getChannel() != null ? t.getChannel().name() : null)
                        .language(t.getLanguage() != null ? t.getLanguage().name() : null)
                        .languageShortCode(t.getLanguage() != null ? t.getLanguage().getShortCode() : null)
                        .build())
                .toList();
        return ResponseEntity.ok(CommonResponse.success("Templates retrieved successfully", dropdownList));
    }
}
