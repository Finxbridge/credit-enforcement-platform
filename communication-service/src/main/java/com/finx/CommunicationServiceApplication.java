package com.finx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Communication Service Application
 * Manages all external communications (SMS, WhatsApp, Email, Payment Links,
 * Dialer)
 *
 * Responsibilities:
 * - SMS sending and tracking
 * - WhatsApp messaging
 * - Email communications
 * - Payment link generation
 * - Dialer integration for telecalling
 * - Communication logs and audit trail
 * - Third-party API integrations
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableKafka
@EnableCaching
@ComponentScan(basePackages = { "com.finx.communication", "com.finx.common" })
@EnableJpaRepositories(basePackages = { "com.finx.communication.repository", "com.finx.common.repository" })
@EntityScan(basePackages = { "com.finx.communication.domain.entity", "com.finx.communication.domain.model",
        "com.finx.common.model", "com.finx.common.domain.entity" })
public class CommunicationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunicationServiceApplication.class, args);
    }
}
