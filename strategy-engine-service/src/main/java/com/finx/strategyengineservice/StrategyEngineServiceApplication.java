package com.finx.strategyengineservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableScheduling
@ComponentScan(basePackages = { "com.finx.strategyengineservice" })
@EnableJpaRepositories(basePackages = { "com.finx.strategyengineservice.repository" })
@EntityScan(basePackages = { "com.finx.strategyengineservice.domain.entity" })
public class StrategyEngineServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(StrategyEngineServiceApplication.class, args);
    }

}
