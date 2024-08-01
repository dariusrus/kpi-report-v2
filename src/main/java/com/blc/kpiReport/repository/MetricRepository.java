package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, Long> {
}
