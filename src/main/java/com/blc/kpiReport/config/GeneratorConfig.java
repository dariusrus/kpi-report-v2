package com.blc.kpiReport.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "generator")
@Getter
@Setter
public class GeneratorConfig {

    private int semaphorePermits; // Default value
    private long retryBackOffPeriod;
    private int retryMaxAttempts;
}
