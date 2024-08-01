package com.blc.kpiReport.service.mc;

import com.blc.kpiReport.config.MicrosoftClarityProperties;
import com.blc.kpiReport.exception.MicrosoftClarityApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicrosoftClarityFetchService {

    private final OkHttpClient okHttpClient;
    private final MicrosoftClarityProperties clarityProperties;
    private final RetryTemplate retryTemplate;

    public String fetchMetricsForPreviousDay(int numOfDays, String apiToken) throws MicrosoftClarityApiException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch metrics for the previous {} days", context.getRetryCount() + 1, numOfDays);

            String url = String.format("%s?numOfDays=%d&dimension1=Device&dimension2=Channel&dimension3=Source",
                clarityProperties.getBaseUrl(), numOfDays);
            log.debug("Fetching metrics from URL: {}", url);

            Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiToken)
                .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorMessage = "Error fetching metrics: " + response;
                    log.error(errorMessage);
                    throw new MicrosoftClarityApiException(errorMessage);
                }
                log.info("Successfully fetched metrics for the previous {} days", numOfDays);
                return response.body().string();
            } catch (IOException e) {
                String errorMessage = "Failed to fetch metrics on attempt " + (context.getRetryCount() + 1);
                log.error("{}: {}", errorMessage, e.getMessage());
                throw new MicrosoftClarityApiException(errorMessage, e);
            }
        });
    }
}
