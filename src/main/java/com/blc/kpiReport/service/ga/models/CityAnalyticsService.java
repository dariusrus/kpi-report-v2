package com.blc.kpiReport.service.ga.models;

import com.blc.kpiReport.repository.ga.CityAnalyticsRepository;
import com.blc.kpiReport.schema.ga.CityAnalytics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityAnalyticsService {

    private final CityAnalyticsRepository repository;

    public CityAnalyticsService(CityAnalyticsRepository repository) {
        this.repository = repository;
    }

    public List<CityAnalytics> saveAll(List<CityAnalytics> cityAnalytics) {
        return repository.saveAll(cityAnalytics);
    }

    public void deleteByGoogleAnalyticsMetricId(Long googleAnalyticsMetricId) {
        repository.deleteByGoogleAnalyticsMetric_Id(googleAnalyticsMetricId);
    }
}
