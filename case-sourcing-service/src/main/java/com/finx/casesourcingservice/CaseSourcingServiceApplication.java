package com.finx.casesourcingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = { "com.finx.casesourcingservice" })
@EnableJpaRepositories(basePackages = { "com.finx.casesourcingservice.repository" })
@EntityScan(basePackages = { "com.finx.casesourcingservice.domain.entity" })
public class CaseSourcingServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(CaseSourcingServiceApplication.class, args);
    }

}
