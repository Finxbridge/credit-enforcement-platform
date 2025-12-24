package com.finx.collectionsservice.client;

import com.finx.collectionsservice.client.dto.CommonResponse;
import com.finx.collectionsservice.client.dto.TemplateResolveRequest;
import com.finx.collectionsservice.client.dto.TemplateResolveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Feign Client for Template Management Service
 * Used for template resolution when sending PTP reminders
 *
 * NOTE: For template dropdowns, frontend should call Template Management Service directly:
 *   - GET /templates/dropdown
 *   - GET /templates/dropdown/{channel}
 */
@FeignClient(name = "template-management-service", url = "${TEMPLATE_SERVICE_URL:http://localhost:8087}", path = "/templates")
public interface TemplateServiceClient {

    /**
     * Resolve template variables and render content for a specific case
     * This is the main method used for sending reminders
     */
    @PostMapping("/{id}/resolve")
    CommonResponse<TemplateResolveResponse> resolveTemplate(
            @PathVariable("id") Long id,
            @RequestBody TemplateResolveRequest request
    );

    /**
     * Get template dropdown options by channel
     * Frontend should call this directly for dropdown
     */
    @GetMapping("/dropdown/{channel}")
    CommonResponse<List<TemplateDropdownDTO>> getTemplatesByChannel(
            @PathVariable("channel") String channel
    );

    /**
     * Simple DTO for template dropdown
     */
    record TemplateDropdownDTO(
            Long id,
            String templateCode,
            String templateName,
            String channel,
            String description
    ) {}
}
