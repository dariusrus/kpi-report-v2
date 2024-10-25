package com.blc.kpiReport.service.ga;

import com.blc.kpiReport.config.GoogleAnalyticsQueryProperties;
import com.blc.kpiReport.exception.GaApiException;
import com.google.api.services.analyticsdata.v1beta.AnalyticsData;
import com.google.api.services.analyticsdata.v1beta.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GaDataFetchService {

    private final AnalyticsData googleAnalyticsData;
    private final GoogleAnalyticsQueryProperties googleAnalyticsQueryProperties;
    private final RetryTemplate retryTemplate;

    public RunReportResponse fetchCityAnalytics(String startDate, String endDate, String gaPropertyId, String countryCode) throws GaApiException {
        try {
            log.debug("Fetching unique site visitors for propertyId: {} from {} to {}", gaPropertyId, startDate, endDate);

            var dateRange = createDateRange(startDate, endDate);
            var metrics = createMetricsList();

            log.debug("Created date range and metrics list");

            RunReportResponse allReportResponses = runReportsForMetrics(metrics, dateRange, gaPropertyId, countryCode);

            log.debug("Fetched all report responses");

            return allReportResponses;
        } catch (IOException e) {
            log.error("Failed to fetch GHL data: {}", e.getMessage());
            throw new GaApiException("Failed to fetch GA data after multiple attempts", e);
        }
    }

    private DateRange createDateRange(String startDate, String endDate) {
        log.debug("Creating date range from {} to {}", startDate, endDate);
        return new DateRange().setStartDate(startDate).setEndDate(endDate);
    }

    private List<Metric> createMetricsList() {
        log.debug("Creating metrics list from query properties");
        var metrics = new ArrayList<Metric>();
        for (var metricName : googleAnalyticsQueryProperties.getMetricNames()) {
            metrics.add(new Metric().setName(metricName));
        }
        log.debug("Metrics list created: {}", metrics);
        return metrics;
    }

    private RunReportResponse runReportsForMetrics(List<Metric> metrics, DateRange dateRange, String gaPropertyId, String countryCode) throws IOException {
        log.debug("Running reports for metrics");

        final RunReportResponse cityAnalytics = retryTemplate.execute(context -> {
            var attempt = context.getRetryCount() + 1;
            log.info("Attempt {} to run report with GA Property ID: {}", attempt, gaPropertyId);

            var reportRequest = new RunReportRequest()
                    .setProperty("properties/" + gaPropertyId)
                    .setDimensions(Collections.singletonList(new Dimension().setName("city")))
                    .setMetrics(metrics)
                    .setDateRanges(Collections.singletonList(dateRange))
                    .setDimensionFilter(new FilterExpression()
                            .setFilter(new Filter()
                                    .setFieldName("countryId")
                                    .setStringFilter(new StringFilter()
                                            .setMatchType("EXACT")
                                            .setValue(countryCode))));

            try {
                // Execute the report request and return the response
                var response = googleAnalyticsData.properties().runReport("properties/" + gaPropertyId, reportRequest).execute();
                log.info("Successfully received response from Google Analytics API");
                return response;
            } catch (IOException e) {
                log.error("Failed to run report on attempt {}", attempt, e);
                throw e;
            }
        });
        return cityAnalytics;
    }
}
