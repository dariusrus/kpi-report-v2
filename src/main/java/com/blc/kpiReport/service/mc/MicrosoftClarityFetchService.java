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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicrosoftClarityFetchService {

    private final OkHttpClient okHttpClient;
    private final MicrosoftClarityProperties clarityProperties;
    private final RetryTemplate retryTemplate;

    public String fetchMetricsForPreviousDay(String apiToken) throws MicrosoftClarityApiException {
        int numOfDays = Integer.parseInt(clarityProperties.getNumOfDays());
        List<String> dimensions = clarityProperties.getDimensions();

        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch metrics for the previous {} days", context.getRetryCount() + 1, numOfDays);

            String dimensionParams = dimensions.stream()
                .map(dimension -> "dimension" + (dimensions.indexOf(dimension) + 1) + "=" + dimension)
                .collect(Collectors.joining("&"));

            String url = String.format("%s?numOfDays=%d&%s",
                clarityProperties.getBaseUrl(), numOfDays, dimensionParams);
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