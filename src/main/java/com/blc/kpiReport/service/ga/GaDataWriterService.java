package com.blc.kpiReport.service.ga;

import com.blc.kpiReport.repository.ga.GoogleAnalyticsRepository;
import com.blc.kpiReport.schema.ga.CityAnalytics;
import com.blc.kpiReport.schema.ga.GoogleAnalyticsMetric;
import com.blc.kpiReport.service.ga.models.CityAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GaDataWriterService {

    private final GoogleAnalyticsRepository repository;
    private final CityAnalyticsService cityAnalyticsService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteGaApiData(GoogleAnalyticsMetric googleAnalyticsMetric) {
        var id = googleAnalyticsMetric.getId();
        cityAnalyticsService.deleteByGoogleAnalyticsMetricId(id);
        log.info("Successfully deleted data for City Analytics with Google Analytics ID: {}", id);
    }

    public GoogleAnalyticsMetric saveGaData(GoogleAnalyticsMetric googleAnalyticsMetric, List<CityAnalytics> cityAnalytics) {
        log.debug("Setting Google Analytics Metric for each CityAnalytics entry and saving data.");

        for (CityAnalytics cityAnalytic : cityAnalytics) {
            cityAnalytic.setGoogleAnalyticsMetric(googleAnalyticsMetric);
        }

        cityAnalyticsService.saveAll(cityAnalytics);
        googleAnalyticsMetric.setCityAnalytics(cityAnalytics);
        log.info("Successfully saved {} CityAnalytics entries for Google Analytics Metric ID: {}",
                cityAnalytics.size(), googleAnalyticsMetric.getId());
        return repository.save(googleAnalyticsMetric);
    }
}
