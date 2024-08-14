package com.blc.kpiReport.service.ga;

import com.blc.kpiReport.config.GoogleAnalyticsQueryProperties;
import com.blc.kpiReport.repository.GoogleAnalyticsRepository;
import com.blc.kpiReport.schema.GoogleAnalyticsMetric;
import com.blc.kpiReport.schema.KpiReport;
import com.google.api.services.analyticsadmin.v1beta.GoogleAnalyticsAdmin;
import com.google.api.services.analyticsadmin.v1beta.model.GoogleAnalyticsAdminV1betaListAccountSummariesResponse;
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
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAnalyticsService {

    private final GoogleAnalyticsAdmin googleAnalyticsAdmin;
    private final AnalyticsData googleAnalyticsData;
    private final GoogleAnalyticsQueryProperties googleAnalyticsQueryProperties;
    private final GoogleAnalyticsRepository repository;
    private final RetryTemplate retryTemplate;

    public GoogleAnalyticsAdminV1betaListAccountSummariesResponse getAccountSummaries() throws IOException {
        log.debug("Fetching account summaries");
        GoogleAnalyticsAdminV1betaListAccountSummariesResponse response = googleAnalyticsAdmin.accountSummaries().list().execute();
        log.info("Fetched account summaries successfully");
        return response;
    }

    public GoogleAnalyticsMetric fetchUniqueSiteVisitors(String startDate, String endDate, String gaPropertyId, KpiReport kpiReport) throws IOException {
        log.debug("Fetching unique site visitors for propertyId: {} from {} to {}", gaPropertyId, startDate, endDate);

        var dateRange = createDateRange(startDate, endDate);
        var metrics = createMetricsList();

        log.debug("Created date range and metrics list");

        var allReportResponses = runReportsForMetrics(metrics, dateRange, gaPropertyId);

        log.debug("Fetched all report responses");

        var uniqueSiteVisitors = extractUniqueSiteVisitors(allReportResponses);

        log.info("Extracted unique site visitors: {}", uniqueSiteVisitors);

        var metric = getOrCreateGoogleAnalyticsMetric(kpiReport, uniqueSiteVisitors);
        return metric;
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

    private List<RunReportResponse> runReportsForMetrics(List<Metric> metrics, DateRange dateRange, String gaPropertyId) throws IOException {
        log.debug("Running reports for metrics");
        var allReportResponses = new ArrayList<RunReportResponse>();

        for (var metric : metrics) {
            retryTemplate.execute(context -> {
                var attempt = context.getRetryCount() + 1;
                log.info("Attempt {} to run report for metric: {} with GA Property ID: {}", attempt, metric.getName(), gaPropertyId);

                var reportRequest = new RunReportRequest()
                    .setProperty("properties/" + gaPropertyId)
                    .setMetrics(Collections.singletonList(metric))
                    .setDateRanges(Collections.singletonList(dateRange));

                log.debug("Running report for metric: {}", metric.getName());

                try {
                    var response = googleAnalyticsData.properties().runReport("properties/" + gaPropertyId, reportRequest).execute();
                    log.info("Successfully received response for metric: {}", metric.getName());
                    allReportResponses.add(response);
                    return response;
                } catch (IOException e) {
                    log.warn("Error fetching report for metric: {}", metric.getName(), e);
                    throw e; // Rethrow exception to trigger retry
                }
            });
        }
        return allReportResponses;
    }


    private Integer extractUniqueSiteVisitors(List<RunReportResponse> allReportResponses) {
        log.debug("Extracting unique site visitors from report responses");

        Integer uniqueSiteVisitors = 0;
        try {
            if (allReportResponses != null && !allReportResponses.isEmpty()) {
                RunReportResponse firstResponse = allReportResponses.get(0);
                if (firstResponse.getRows() != null && !firstResponse.getRows().isEmpty()) {
                    Row firstRow = firstResponse.getRows().get(0);
                    if (firstRow.getMetricValues() != null && !firstRow.getMetricValues().isEmpty()) {
                        String value = firstRow.getMetricValues().get(0).getValue();
                        uniqueSiteVisitors = value != null ? Integer.valueOf(value) : 0;
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error("Error parsing unique site visitors value", e);
        } catch (Exception e) {
            log.error("Unexpected error while extracting unique site visitors", e);
        }

        log.debug("Unique site visitors extracted: {}", uniqueSiteVisitors);
        return uniqueSiteVisitors;
    }


    private GoogleAnalyticsMetric getOrCreateGoogleAnalyticsMetric(KpiReport kpiReport, Integer uniqueSiteVisitors) {
        log.debug("Getting or creating Google Analytics metric for KpiReport ID: {}", kpiReport.getId());
        Optional<GoogleAnalyticsMetric> existingMetric = repository.findByKpiReport_Id(kpiReport.getId());

        if (existingMetric.isPresent()) {
            log.info("Found existing Google Analytics metric for KpiReport ID: {}", kpiReport.getId());
            var googleAnalyticsMetric = existingMetric.get();
            googleAnalyticsMetric.setUniqueSiteVisitors(uniqueSiteVisitors);
            return repository.save(googleAnalyticsMetric);
        } else {
            GoogleAnalyticsMetric newMetric = GoogleAnalyticsMetric.builder()
                .uniqueSiteVisitors(uniqueSiteVisitors)
                .kpiReport(kpiReport)
                .build();

            log.info("Saving new Google Analytics metric for KpiReport ID: {}", kpiReport.getId());
            return repository.save(newMetric);
        }
    }
}
