package com.finx.templatemanagementservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient Configuration for reactive HTTP calls
 */
@Configuration
public class WebClientConfig {

    /**
     * Load-balanced WebClient for inter-service communication
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * WebClient instance for communication service
     */
    @Bean
    public WebClient communicationServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://communication-service/api/v1")
                .build();
    }
}
