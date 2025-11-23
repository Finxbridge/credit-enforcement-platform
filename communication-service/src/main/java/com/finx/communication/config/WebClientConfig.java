package com.finx.communication.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finx.common.service.ConfigCacheService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient Configuration for Third-Party Integration APIs
 * - Database-driven timeout configuration (from system_config)
 * - Singleton WebClient instance with connection pooling
 * - Custom timeouts and keep-alive configuration
 * - Request/Response logging
 * - Error handling
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final ConfigCacheService configCacheService;

    private static final int MAX_IN_MEMORY_SIZE = 16 * 1024 * 1024; // 16MB

    // Default fallback values if config not found
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final int DEFAULT_READ_TIMEOUT = 60; // 60 seconds
    private static final int DEFAULT_WRITE_TIMEOUT = 60; // 60 seconds
    private static final int DEFAULT_RESPONSE_TIMEOUT = 60; // 60 seconds
    private static final int DEFAULT_MAX_CONNECTIONS = 500;
    private static final int DEFAULT_PENDING_ACQUIRE_TIMEOUT = 45000; // 45 seconds
    private static final int DEFAULT_MAX_IDLE_TIME = 20000; // 20 seconds
    private static final int DEFAULT_MAX_LIFE_TIME = 60000; // 60 seconds

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        return objectMapper;
    }

    /**
     * Creates or returns a singleton {@link WebClient} instance with custom
     * timeouts,
     * connection pooling, and keep-alive configuration for optimal performance.
     * Configuration loaded from system_config table (cached).
     *
     * @return configured WebClient bean
     */
    @SuppressWarnings("null")
    @Bean
    public WebClient webClient(ObjectMapper objectMapper) {
        log.info("Initializing WebClient with database-driven configuration");

        // Load timeout configuration from database (cached)
        int connectionTimeout = configCacheService.getIntConfig("WEBCLIENT_CONNECTION_TIMEOUT",
                DEFAULT_CONNECTION_TIMEOUT);
        int readTimeout = configCacheService.getIntConfig("WEBCLIENT_READ_TIMEOUT", DEFAULT_READ_TIMEOUT);
        int writeTimeout = configCacheService.getIntConfig("WEBCLIENT_WRITE_TIMEOUT", DEFAULT_WRITE_TIMEOUT);
        int responseTimeout = configCacheService.getIntConfig("WEBCLIENT_RESPONSE_TIMEOUT", DEFAULT_RESPONSE_TIMEOUT);

        // Load connection pool configuration from database (cached)
        int maxConnections = configCacheService.getIntConfig("WEBCLIENT_MAX_CONNECTIONS", DEFAULT_MAX_CONNECTIONS);
        int pendingAcquireTimeout = configCacheService.getIntConfig("WEBCLIENT_PENDING_ACQUIRE_TIMEOUT",
                DEFAULT_PENDING_ACQUIRE_TIMEOUT);
        int maxIdleTime = configCacheService.getIntConfig("WEBCLIENT_MAX_IDLE_TIME", DEFAULT_MAX_IDLE_TIME);
        int maxLifeTime = configCacheService.getIntConfig("WEBCLIENT_MAX_LIFE_TIME", DEFAULT_MAX_LIFE_TIME);

        log.info("WebClient Timeouts - Connection: {}ms, Read: {}s, Write: {}s, Response: {}s",
                connectionTimeout, readTimeout, writeTimeout, responseTimeout);
        log.info("WebClient Connection Pool - Max: {}, Acquire Timeout: {}ms, Idle: {}ms, Lifetime: {}ms",
                maxConnections, pendingAcquireTimeout, maxIdleTime, maxLifeTime);

        // Configure connection provider with pooling
        ConnectionProvider connectionProvider = ConnectionProvider.builder("integration-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofMillis(pendingAcquireTimeout))
                .maxIdleTime(Duration.ofMillis(maxIdleTime))
                .maxLifeTime(Duration.ofMillis(maxLifeTime))
                .evictInBackground(Duration.ofSeconds(30))
                .build();

        // Configure HttpClient with timeouts and connection settings
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.SECONDS)))
                .responseTimeout(Duration.ofSeconds(responseTimeout))
                .resolver(spec -> spec.queryTimeout(Duration.ofSeconds(5)));

        // Configure exchange strategies for large payloads
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleError())
                .build();
    }

    /**
     * Request logging filter
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers()
                        .forEach((name, values) -> values.forEach(value -> log.debug("{}: {}", name, value)));
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * Response logging filter
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("Response status: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Error handling filter
     */
    private ExchangeFilterFunction handleError() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("API Error Response: Status={}, Body={}",
                                    clientResponse.statusCode(), errorBody);
                            return Mono.just(clientResponse);
                        });
            }
            return Mono.just(clientResponse);
        });
    }
}
