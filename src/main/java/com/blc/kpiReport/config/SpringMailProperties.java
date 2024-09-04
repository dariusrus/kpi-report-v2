package com.blc.kpiReport.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@RequiredArgsConstructor
@Getter
@Setter
public class SpringMailProperties {
    private String host;
    private int port;
    private Account account;
    private ReportNotification reportNotification;

    @Getter
    @Setter
    public static class Account {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class ReportNotification {
        private String recipients;
    }
}