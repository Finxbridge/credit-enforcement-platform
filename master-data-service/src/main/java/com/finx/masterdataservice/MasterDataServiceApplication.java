package com.finx.masterdataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = { "com.finx.masterdataservice" })
@EnableJpaRepositories(basePackages = { "com.finx.masterdataservice.repository" })
@EntityScan(basePackages = { "com.finx.masterdataservice.domain.entity" })
public class MasterDataServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(MasterDataServiceApplication.class, args);
    }

}
