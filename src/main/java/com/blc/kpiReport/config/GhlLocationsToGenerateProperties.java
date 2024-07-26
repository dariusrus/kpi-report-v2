package com.blc.kpiReport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "kpi-report")
public class GhlLocationsToGenerateProperties {
    private List<String> ghlLocationIds;
}

