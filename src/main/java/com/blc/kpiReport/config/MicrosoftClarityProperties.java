package com.blc.kpiReport.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "microsoft-clarity")
@Getter
@Setter
public class MicrosoftClarityProperties {
    private String baseUrl;
    private String numOfDays;
}

