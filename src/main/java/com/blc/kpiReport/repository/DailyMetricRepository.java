package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.DailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyMetricRepository extends JpaRepository<DailyMetric, Long> {
    Optional<DailyMetric> findByKpiReport_IdAndDay(Long kpiReportId, int day);
}
