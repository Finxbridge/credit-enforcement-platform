package com.finx.collectionsservice.client;

import com.finx.collectionsservice.client.dto.WhatsAppSendRequest;
import com.finx.collectionsservice.client.dto.WhatsAppSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Client for Communication Service APIs
 * Handles WhatsApp, SMS, and Email integrations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunicationServiceClient {

    @Qualifier("communicationServiceClient")
    private final WebClient communicationServiceClient;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * Send WhatsApp message via Communication Service
     *
     * @param request WhatsApp send request with template and recipients
     * @return Response with message IDs and status
     */
    public WhatsAppSendResponse sendWhatsApp(WhatsAppSendRequest request) {
        log.info("Sending WhatsApp message with template: {} to {} recipients",
                request.getTemplateId(), request.getTo().size());

        try {
            WhatsAppSendResponse response = communicationServiceClient
                    .post()
                    .uri("/comm/whatsapp/send")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WhatsAppSendResponse.class)
                    .timeout(TIMEOUT)
                    .block();

            log.info("WhatsApp message sent successfully. Status: {}, MessageIds: {}",
                    response != null ? response.getStatus() : "null",
                    response != null ? response.getMessageIds() : "null");

            return response;

        } catch (WebClientResponseException e) {
            log.error("WhatsApp API error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    /**
     * Send WhatsApp message asynchronously
     *
     * @param request WhatsApp send request
     * @return Mono with response
     */
    public Mono<WhatsAppSendResponse> sendWhatsAppAsync(WhatsAppSendRequest request) {
        log.info("Sending WhatsApp message async with template: {} to {} recipients",
                request.getTemplateId(), request.getTo().size());

        return communicationServiceClient
                .post()
                .uri("/comm/whatsapp/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WhatsAppSendResponse.class)
                .timeout(TIMEOUT)
                .doOnSuccess(response -> log.info("WhatsApp message sent successfully. Status: {}",
                        response != null ? response.getStatus() : "null"))
                .doOnError(error -> log.error("Failed to send WhatsApp message: {}", error.getMessage()));
    }

    /**
     * Health check for communication service
     */
    public boolean isHealthy() {
        try {
            communicationServiceClient
                    .get()
                    .uri("/actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Communication service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
