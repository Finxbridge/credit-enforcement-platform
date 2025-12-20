package com.finx.noticemanagementservice.client;

import com.finx.noticemanagementservice.domain.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceClient {

    private final RestTemplate restTemplate;

    @Value("${TEMPLATE_MANAGEMENT_SERVICE_URL:http://localhost:8087}")
    private String baseUrl;

    public CommonResponse<Map<String, Object>> getTemplateById(Long templateId) {
        log.info("Fetching template with id: {}", templateId);
        return get("/templates/" + templateId);
    }

    public CommonResponse<Map<String, Object>> getTemplateByCode(String templateCode) {
        log.info("Fetching template with code: {}", templateCode);
        return get("/templates/code/" + templateCode);
    }

    public CommonResponse<Map<String, Object>> resolveTemplate(Long templateId, Map<String, Object> variables) {
        log.info("Resolving template {} with variables", templateId);
        return post("/templates/" + templateId + "/resolve", variables);
    }

    public CommonResponse<Map<String, Object>> resolveTemplateByCode(String templateCode, Map<String, Object> variables) {
        log.info("Resolving template {} with variables", templateCode);
        return post("/templates/code/" + templateCode + "/resolve", variables);
    }

    public String generateNoticeContent(Long templateId, Long caseId) {
        log.info("Generating notice content for template {} and case {}", templateId, caseId);
        try {
            Map<String, Object> request = Map.of(
                    "templateId", templateId,
                    "caseId", caseId
            );
            CommonResponse<Map<String, Object>> response = post("/templates/render/notice", request);
            if (response != null && response.isSuccess() && response.getData() != null) {
                Object content = response.getData().get("content");
                return content != null ? content.toString() : null;
            }
        } catch (Exception e) {
            log.error("Error generating notice content: {}", e.getMessage());
        }
        return null;
    }

    private CommonResponse<Map<String, Object>> get(String path) {
        try {
            return restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<CommonResponse<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            log.error("Error calling template service: {}", e.getMessage());
            return CommonResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to call template service: " + e.getMessage())
                    .build();
        }
    }

    private CommonResponse<Map<String, Object>> post(String path, Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            return restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<CommonResponse<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            log.error("Error calling template service: {}", e.getMessage());
            return CommonResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Failed to call template service: " + e.getMessage())
                    .build();
        }
    }
}
