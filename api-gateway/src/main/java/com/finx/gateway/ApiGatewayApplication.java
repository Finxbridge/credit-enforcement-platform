package com.finx.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application
 * Single Entry Point for all CMS-NMS Platform Services
 * Handles routing, authentication, rate limiting, and circuit breaking
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
