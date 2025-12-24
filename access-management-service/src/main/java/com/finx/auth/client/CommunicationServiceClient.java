package com.finx.auth.client;

import com.finx.auth.config.FeignConfig;
import com.finx.common.domain.dto.InternalEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "communication-service", url = "${COMMUNICATION_SERVICE_URL:http://localhost:8085}", fallback = CommunicationServiceFallback.class, configuration = FeignConfig.class)
public interface CommunicationServiceClient {

    @PostMapping("/comm/email/sendgrid/send")
    EmailResponse sendEmail(@RequestBody EmailSendRequest request);

    @PostMapping("/comm/email/msg91/send")
    void sendMsg91Email(@RequestBody InternalEmailRequest request);

    // DTOs for the client
    record EmailSendRequest(String to, String subject, String bodyText, String bodyHtml, String templateId) {
    }

    record EmailResponse(String status, String message) {
    }
}
