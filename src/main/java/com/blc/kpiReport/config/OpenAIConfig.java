package com.blc.kpiReport.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Getter
public class OpenAIConfig {

    @Value("${openai.api.secret-key}")
    private String apiKey;

    @Value("${openai.api.base-url}")
    private String baseUrl;

    private final OkHttpClient okHttpClient;
}
