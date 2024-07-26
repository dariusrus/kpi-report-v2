package com.blc.kpiReport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "google-analytics")
public class GoogleAnalyticsQueryProperties {
    private List<String> dimensionNames;
    private List<String> metricNames;
}

