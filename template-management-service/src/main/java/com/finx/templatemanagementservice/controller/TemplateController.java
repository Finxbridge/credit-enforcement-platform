package com.finx.templatemanagementservice.controller;

import com.finx.templatemanagementservice.domain.dto.*;
import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.service.TemplateService;
import com.finx.templatemanagementservice.service.TemplateVariableResolverService;
import com.finx.templatemanagementservice.service.TemplateRenderingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Template Management
 */
@Slf4j
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@Tag(name = "Template Management", description = "APIs for managing communication templates")
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateVariableResolverService variableResolverService;
    private final TemplateRenderingService renderingService;

    @PostMapping
    @Operation(summary = "Create template", description = "Create a new communication template with variables")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {
        log.info("POST /api/v1/templates - Creating template: {}", request.getTemplateCode());
        TemplateDetailDTO template = templateService.createTemplate(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("Template created successfully", template));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template", description = "Get template details by ID")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> getTemplate(@PathVariable Long id) {
        log.info("GET /api/v1/templates/{} - Fetching template", id);
        TemplateDetailDTO template = templateService.getTemplate(id);
        return ResponseEntity.ok(CommonResponse.success("Template retrieved successfully", template));
    }

    @GetMapping("/code/{templateCode}")
    @Operation(summary = "Get template by code", description = "Get template details by template code")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> getTemplateByCode(
            @PathVariable String templateCode) {
        log.info("GET /api/v1/templates/code/{} - Fetching template by code", templateCode);
        TemplateDetailDTO template = templateService.getTemplateByCode(templateCode);
        return ResponseEntity.ok(CommonResponse.success("Template retrieved successfully", template));
    }

    @GetMapping
    @Operation(summary = "Get all templates", description = "Get list of all active templates")
    public ResponseEntity<CommonResponse<List<TemplateDTO>>> getAllTemplates() {
        log.info("GET /api/v1/templates - Fetching all templates");
        List<TemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(CommonResponse.success("Templates retrieved successfully", templates));
    }

    @GetMapping("/channel/{channel}")
    @Operation(summary = "Get templates by channel", description = "Get templates filtered by communication channel")
    public ResponseEntity<CommonResponse<List<TemplateDTO>>> getTemplatesByChannel(
            @PathVariable ChannelType channel) {
        log.info("GET /api/v1/templates/channel/{} - Fetching templates by channel", channel);
        List<TemplateDTO> templates = templateService.getTemplatesByChannel(channel);
        return ResponseEntity.ok(CommonResponse.success("Templates retrieved successfully", templates));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template", description = "Update template metadata")
    public ResponseEntity<CommonResponse<TemplateDetailDTO>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateRequest request) {
        log.info("PUT /api/v1/templates/{} - Updating template", id);
        TemplateDetailDTO template = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(CommonResponse.success("Template updated successfully", template));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template", description = "Soft delete a template")
    public ResponseEntity<CommonResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("DELETE /api/v1/templates/{} - Deleting template", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(CommonResponse.successMessage("Template deleted successfully"));
    }

    @GetMapping("/{id}/variables")
    @Operation(summary = "Get template variables", description = "Get all variables for a template")
    public ResponseEntity<CommonResponse<List<TemplateVariableDTO>>> getTemplateVariables(
            @PathVariable Long id) {
        log.info("GET /api/v1/templates/{}/variables - Fetching template variables", id);
        List<TemplateVariableDTO> variables = templateService.getTemplateVariables(id);
        return ResponseEntity.ok(CommonResponse.success("Variables retrieved successfully", variables));
    }

    @GetMapping("/search")
    @Operation(summary = "Search templates", description = "Search templates by keyword")
    public ResponseEntity<CommonResponse<List<TemplateDTO>>> searchTemplates(
            @RequestParam String keyword) {
        log.info("GET /api/v1/templates/search?keyword={} - Searching templates", keyword);
        List<TemplateDTO> templates = templateService.searchTemplates(keyword);
        return ResponseEntity.ok(CommonResponse.success("Search completed successfully", templates));
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "Sync with provider", description = "Sync template with communication service provider")
    public ResponseEntity<CommonResponse<Void>> syncWithProvider(@PathVariable Long id) {
        log.info("POST /api/v1/templates/{}/sync - Syncing with provider", id);
        templateService.syncWithProvider(id);
        return ResponseEntity.ok(CommonResponse.successMessage("Template synced successfully"));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve template variables", description = "Resolve template variables and render content for a specific case")
    public ResponseEntity<CommonResponse<TemplateResolveResponse>> resolveTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateResolveRequest request) {
        log.info("POST /api/v1/templates/{}/resolve - Resolving template for case: {}", id, request.getCaseId());

        // Resolve variables from case data
        Map<String, Object> resolvedVariables = variableResolverService.resolveVariablesForTemplate(
                id, request.getCaseId(), request.getAdditionalContext()
        );

        // Render template content
        String renderedContent = renderingService.renderTemplateForCase(
                id, request.getCaseId(), request.getAdditionalContext()
        );

        // Get template for subject rendering
        TemplateDetailDTO template = templateService.getTemplate(id);
        String renderedSubject = null;

        // Render subject if available (content is a single object, not a list)
        if (template.getContent() != null) {
            String subject = template.getContent().getSubject();
            if (subject != null && !subject.isEmpty()) {
                renderedSubject = renderingService.renderSubject(subject, resolvedVariables);
            }
        }

        TemplateResolveResponse response = TemplateResolveResponse.builder()
                .templateId(id)
                .templateCode(template.getTemplateCode())
                .resolvedVariables(resolvedVariables)
                .renderedContent(renderedContent)
                .subject(renderedSubject)
                .build();

        return ResponseEntity.ok(CommonResponse.success("Template resolved successfully", response));
    }
}
