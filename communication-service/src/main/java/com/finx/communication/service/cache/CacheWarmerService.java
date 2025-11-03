package com.finx.communication.service.cache;

import com.finx.common.service.IntegrationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmerService implements ApplicationRunner {

    private final IntegrationCacheService integrationCacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting cache warming process...");
        integrationCacheService.loadAllIntegrations();
        log.info("Cache warming process completed.");
    }
}
