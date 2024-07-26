package com.blc.kpiReport.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analyticsadmin.v1beta.GoogleAnalyticsAdmin;
import com.google.api.services.analyticsadmin.v1beta.GoogleAnalyticsAdminScopes;
import com.google.api.services.analyticsdata.v1beta.AnalyticsData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleAnalyticsGA4Client {

    private static final String APPLICATION_NAME = "BLC API Integrations";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google-analytics.credentials-file}")
    private String credentialsFilePath;

    @Bean
    public GoogleAnalyticsAdmin googleAnalyticsAdmin() throws IOException, GeneralSecurityException {
        // Initialize the Analytics Admin API
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new GoogleAnalyticsAdmin.Builder(httpTransport, JSON_FACTORY, getCredentials())
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    @Bean
    public AnalyticsData googleAnalyticsData() throws IOException, GeneralSecurityException {
        // Initialize the Analytics Data API
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new AnalyticsData.Builder(httpTransport, JSON_FACTORY, getCredentials())
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    private GoogleCredential getCredentials() throws IOException {
        return GoogleCredential.fromStream(
                getClass().getResourceAsStream(credentialsFilePath))
            .createScoped(Collections.singleton(GoogleAnalyticsAdminScopes.ANALYTICS_READONLY));
    }
}
