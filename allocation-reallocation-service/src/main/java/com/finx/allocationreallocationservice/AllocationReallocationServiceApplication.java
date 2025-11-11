package com.finx.allocationreallocationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
@EnableAsync
@ComponentScan(basePackages = { "com.finx.allocationreallocationservice" })
@EnableJpaRepositories(basePackages = { "com.finx.allocationreallocationservice.repository" })
@EntityScan(basePackages = { "com.finx.allocationreallocationservice.domain.entity" })
public class AllocationReallocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllocationReallocationServiceApplication.class, args);
    }

}
