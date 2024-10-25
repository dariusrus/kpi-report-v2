package com.blc.kpiReport.repository.ga;

import com.blc.kpiReport.schema.ga.CityAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityAnalyticsRepository extends JpaRepository<CityAnalytics, Long> {
    void deleteByGoogleAnalyticsMetric_Id(Long googleAnalyticsMetricId);
}
