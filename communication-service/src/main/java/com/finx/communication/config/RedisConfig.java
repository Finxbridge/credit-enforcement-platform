package com.finx.communication.config;

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
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.util.Set;

import java.time.Duration;

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

        @Value("${spring.data.redis.host:localhost}")
        private String redisHost;

        @Value("${spring.data.redis.port:6379}")
        private int redisPort;

        @Value("${spring.data.redis.username:}")
        private String redisUsername;

        @Value("${spring.data.redis.password:}")
        private String redisPassword;

        @Value("${spring.data.redis.database:0}")
        private int redisDatabase;

        @Value("${spring.data.redis.ssl.enabled:false}")
        private boolean sslEnabled;

        /**
         * Redis Connection Factory using Lettuce
         */
        @SuppressWarnings("null")
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
                log.info("Initializing Redis connection to {}:{} database: {} SSL: {}", redisHost, redisPort, redisDatabase, sslEnabled);

                RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
                config.setHostName(redisHost);
                config.setPort(redisPort);
                config.setDatabase(redisDatabase);

                if (redisUsername != null && !redisUsername.isEmpty()) {
                        config.setUsername(redisUsername);
                }

                if (redisPassword != null && !redisPassword.isEmpty()) {
                        config.setPassword(redisPassword);
                }

                LettuceClientConfiguration clientConfig;

                if (sslEnabled) {
                        log.info("Configuring SSL/TLS for Redis connection");
                        clientConfig = LettuceClientConfiguration.builder()
                                .commandTimeout(Duration.ofSeconds(10))
                                .useSsl()
                                .build();
                } else {
                        clientConfig = LettuceClientConfiguration.builder()
                                .commandTimeout(Duration.ofSeconds(10))
                                .build();
                }

                LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
                log.info("Redis connection factory created successfully");
                return factory;
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
        @SuppressWarnings("null")
        @Bean
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
        @SuppressWarnings("null")
        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
                log.info(
                                "Configuring RedisCacheManager with GenericJackson2JsonRedisSerializer for ThirdPartyIntegrationMaster");

                RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60)) // Default cache expiration
                                .disableCachingNullValues()
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer(
                                                                redisObjectMapper)));

                return RedisCacheManager.builder(connectionFactory)
                                .initialCacheNames(Set.of("integrationConfig")) // Specify the cache name
                                .withCacheConfiguration("integrationConfig", cacheConfiguration)
                                .build();
        }
}
