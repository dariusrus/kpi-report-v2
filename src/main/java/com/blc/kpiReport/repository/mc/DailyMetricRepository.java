package com.blc.kpiReport.repository.mc;

import com.blc.kpiReport.schema.mc.DailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyMetricRepository extends JpaRepository<DailyMetric, Long> {
    Optional<DailyMetric> findByKpiReport_IdAndDay(Long kpiReportId, int day);
}
