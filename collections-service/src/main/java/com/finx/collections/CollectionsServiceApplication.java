package com.finx.collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.finx.collections.service.client")
@ComponentScan(basePackages = {"com.finx.collections", "com.finx.common"})
public class CollectionsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CollectionsServiceApplication.class, args);
    }
}
