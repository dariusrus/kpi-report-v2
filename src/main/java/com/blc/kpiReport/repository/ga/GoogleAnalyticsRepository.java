package com.blc.kpiReport.repository.ga;

import com.blc.kpiReport.schema.ga.GoogleAnalyticsMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleAnalyticsRepository extends JpaRepository<GoogleAnalyticsMetric, Long> {
    Optional<GoogleAnalyticsMetric> findByKpiReport_Id(Long kpiReportId);
}
