package com.blc.kpiReport.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "go-high-level")
@RequiredArgsConstructor
@Getter
@Setter
public class GoHighLevelProperties {
    private String clientId;
    private String clientSecret;
}
