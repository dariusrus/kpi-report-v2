package com.blc.kpiReport.service.ga;

import com.blc.kpiReport.exception.GaApiException;
import com.blc.kpiReport.repository.ga.GoogleAnalyticsRepository;
import com.blc.kpiReport.schema.ga.GoogleAnalyticsMetric;
import com.blc.kpiReport.schema.KpiReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAnalyticsService {

    private final GaDataFetchService fetchService;
    private final GaDataProcessorService processorService;
    private final GaDatWriterService writerService;
    private final GoogleAnalyticsRepository repository;

    public GoogleAnalyticsMetric fetchGaReporting(String startDate, String endDate, String gaPropertyId, String gaCountryCode, KpiReport kpiReport) throws GaApiException {
        // Read data from Google Analytics
        var rawCityAnalytics = fetchService.fetchCityAnalytics(startDate, endDate, gaPropertyId, gaCountryCode);

        // Process from raw data to report data
        var cityAnalytics = processorService.processCityAnalytics(rawCityAnalytics);
        var totalUniqueSiteVisitors = processorService.aggregateUniqueSiteVisitors(cityAnalytics);

        // Persist monthly report data to DB
        var googleAnalyticsMetric = getOrCreateGoogleAnalyticsMetric(kpiReport, totalUniqueSiteVisitors);
        return writerService.saveGaData(googleAnalyticsMetric, cityAnalytics);
    }

    private GoogleAnalyticsMetric getOrCreateGoogleAnalyticsMetric(KpiReport kpiReport, Integer uniqueSiteVisitors) {
        log.debug("Getting or creating Google Analytics metric for KpiReport ID: {}", kpiReport.getId());
        Optional<GoogleAnalyticsMetric> existingMetric = repository.findByKpiReport_Id(kpiReport.getId());

        if (existingMetric.isPresent()) {
            log.info("Found existing Google Analytics metric for KpiReport ID: {}", kpiReport.getId());
            var googleAnalyticsMetric = existingMetric.get();
            googleAnalyticsMetric.setUniqueSiteVisitors(uniqueSiteVisitors);
            writerService.deleteGaApiData(googleAnalyticsMetric);
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
