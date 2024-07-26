package com.blc.kpiReport.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


/**
 * Configuration class for defining security settings.
 * Enables web security and provides customization for API key authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${spring.application.name}")
    private String appName;
    public static final String API_KEY_HEADER = "X-BLC-API-KEY";

    /**
     * Configures custom OpenAPI documentation for the application to enable authorization.
     *
     * @return the custom OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Monthly KPI Report Generator API for BLC")
                .description("API definitions for generating and retrieving monthly KPI reports for Builder Lead Converter (BLC). This API provides endpoints for generating KPI reports by location and retrieving report statuses based on report IDs or specified date ranges."))
            .components(new Components()
                .addSecuritySchemes(API_KEY_HEADER, new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(API_KEY_HEADER)))
            .addSecurityItem(new SecurityRequirement().addList(API_KEY_HEADER));
    }

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured security filter chain
     * @throws Exception if an error occurs while configuring the filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(apiKeyFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}

