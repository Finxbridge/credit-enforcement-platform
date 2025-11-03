package com.finx.caseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.finx.caseservice.service.client")
@ComponentScan(basePackages = {"com.finx.caseservice", "com.finx.common"})
public class CaseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}