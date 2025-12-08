package com.finx.strategyengineservice.client;

import com.finx.strategyengineservice.client.dto.TemplateDetailDTO;
import com.finx.strategyengineservice.client.dto.TemplateResolveRequest;
import com.finx.strategyengineservice.client.dto.TemplateResolveResponse;
import com.finx.strategyengineservice.domain.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Template Management Service
 */
@FeignClient(name = "template-management-service", url = "${TEMPLATE_SERVICE_URL:http://template-management-service:8087}", path = "/api/v1/templates")
public interface TemplateServiceClient {

    /**
     * Get template details by ID
     */
    @GetMapping("/{id}")
    CommonResponse<TemplateDetailDTO> getTemplate(@PathVariable("id") Long id);

    /**
     * Get template details by template code
     */
    @GetMapping("/code/{templateCode}")
    CommonResponse<TemplateDetailDTO> getTemplateByCode(@PathVariable("templateCode") String templateCode);

    /**
     * Resolve template variables and render content for a specific case
     */
    @PostMapping("/{id}/resolve")
    CommonResponse<TemplateResolveResponse> resolveTemplate(
            @PathVariable("id") Long id,
            @RequestBody TemplateResolveRequest request
    );
}
