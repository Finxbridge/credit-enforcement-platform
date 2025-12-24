package com.finx.agencymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

/**
 * Agency Management Service Application
 * Manages collection agencies, onboarding, approval workflows, and agency users
 *
 * @author Naveen Manyam
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = { "com.finx.agencymanagement" })
@EnableJpaRepositories(basePackages = { "com.finx.agencymanagement.repository" })
@EntityScan(basePackages = { "com.finx.agencymanagement.domain.entity" })
public class AgencyManagementServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(AgencyManagementServiceApplication.class, args);
    }
}
