package com.finx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.TimeZone;

/**
 * Authentication Service Application
 * Handles JWT-based authentication, OTP verification, and session management
 *
 * Responsibilities:
 * - User login/logout
 * - JWT token generation and validation
 * - OTP generation and verification
 * - Password reset flows
 * - Session management
 * - Token refresh
 *
 * Dependencies:
 * - User Service: Get user details, roles, permissions via Feign
 * - Communication Service: Send OTP via SMS/Email
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
@ComponentScan(basePackages = { "com.finx.auth", "com.finx.management",
                "com.finx.common" })
@EnableJpaRepositories(basePackages = { "com.finx.auth.repository", "com.finx.management.repository",
                "com.finx.common.repository" })
@EntityScan(basePackages = { "com.finx.auth.domain.entity", "com.finx.common.model", "com.finx.common.domain.entity",
                "com.finx.management.domain.entity" })
@EnableMethodSecurity
public class AccessManagementServiceApplication {

        public static void main(String[] args) {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
                SpringApplication.run(AccessManagementServiceApplication.class, args);
        }
}
