package com.blc.kpiReport.repository.mc;

import com.blc.kpiReport.schema.mc.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, Long> {
}
