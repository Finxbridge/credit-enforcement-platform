package com.finx.casesourcingservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration for Cache Layer
 *
 * Purpose: Configure Redis connection, serialization, and RedisTemplate
 * Layer: Layer 2 - Cache & Redis Management
 *
 * Features:
 * - Lettuce client configuration
 * - JSON serialization with Jackson
 * - RedisTemplate for generic operations
 * - Connection pooling
 *
 * @author Naveen Manyam
 * @version 1.0
 */
@Slf4j
@Configuration
public class RedisConfig {

    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;
    private final int redisDatabase;

    public RedisConfig(
            @Value("${spring.data.redis.host:localhost}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort,
            @Value("${spring.data.redis.password:}") String redisPassword,
            @Value("${spring.data.redis.database:0}") int redisDatabase
    ) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
        this.redisDatabase = redisDatabase;
    }

        /**
         * Redis Connection Factory using Lettuce
         */
        @Bean
        @SuppressWarnings("null")
        public RedisConnectionFactory redisConnectionFactory() {
                log.info("Initializing Redis connection to {}:{} database: {}", redisHost, redisPort, redisDatabase);

                RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
                config.setHostName(redisHost);
                config.setPort(redisPort);
                config.setDatabase(redisDatabase);

                if (!redisPassword.isEmpty()) {
                        config.setPassword(redisPassword);
                }

                return new LettuceConnectionFactory(config);
        }

        /**
         * ObjectMapper for Redis JSON Serialization ONLY
         * Configured to handle Java 8 time types and polymorphic types
         *
         * IMPORTANT: This ObjectMapper includes @class fields for polymorphic type
         * handling
         * and should ONLY be used by RedisTemplate, NOT for HTTP serialization
         */
        @Bean(name = "redisObjectMapper")
        public ObjectMapper redisObjectMapper() {
                log.info("Creating RedisObjectMapper with @class type information for Redis serialization");

                ObjectMapper objectMapper = new ObjectMapper();

                // Register Java Time Module for LocalDateTime, LocalDate, etc.
                objectMapper.registerModule(new JavaTimeModule());

                // Disable timestamps - use ISO-8601 format
                objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Enable polymorphic type handling for inheritance (adds @class fields)
                objectMapper.activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder()
                                                .allowIfBaseType(Object.class)
                                                .build(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                log.info("RedisObjectMapper created successfully with @class fields for Redis");
                return objectMapper;
        }

        /**
         * RedisTemplate with String key and JSON value serialization
         */
        @Bean
        @SuppressWarnings("null")
        public RedisTemplate<String, Object> redisTemplate(
                        RedisConnectionFactory connectionFactory) {

                log.info("Configuring RedisTemplate with JSON serialization using redisObjectMapper");

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Key serializer - String
                StringRedisSerializer stringSerializer = new StringRedisSerializer();
                template.setKeySerializer(stringSerializer);
                template.setHashKeySerializer(stringSerializer);

                // Value serializer - JSON with Redis-specific ObjectMapper
                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                redisObjectMapper());
                template.setValueSerializer(jsonSerializer);
                template.setHashValueSerializer(jsonSerializer);

                template.setEnableTransactionSupport(false);
                template.afterPropertiesSet();

                log.info("RedisTemplate configured successfully with redisObjectMapper");
                return template;
        }

        /**
         * Configures the Redis Cache Manager to use GenericJackson2JsonRedisSerializer
         * for cache values, ensuring proper serialization of non-Serializable objects.
         */
        @Bean
        @SuppressWarnings("null")
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
                log.info("Configuring RedisCacheManager with specific TTLs for various caches");

                RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60)) // Default cache expiration: 60 minutes
                                .disableCachingNullValues()
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer(
                                                                redisObjectMapper)));

                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // Specific cache configurations for case-sourcing-service
                cacheConfigurations.put("caseCache", defaultCacheConfiguration.entryTtl(Duration.ofHours(4)));
                cacheConfigurations.put("batchCache", defaultCacheConfiguration.entryTtl(Duration.ofHours(2)));
                cacheConfigurations.put("masterDataCache", defaultCacheConfiguration.entryTtl(Duration.ofHours(6)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultCacheConfiguration)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}
