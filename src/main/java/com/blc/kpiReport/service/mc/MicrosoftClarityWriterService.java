package com.blc.kpiReport.service.mc;

import com.blc.kpiReport.repository.mc.DailyMetricRepository;
import com.blc.kpiReport.repository.mc.MetricRepository;
import com.blc.kpiReport.schema.mc.DailyMetric;
import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.mc.Metric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MicrosoftClarityWriterService {
    private final DailyMetricRepository dailyMetricRepository;
    private final MetricRepository metricRepository;

    public DailyMetric getOrCreateDailyMetric(KpiReport kpiReport, int day) {
        var existingDailyMetric = dailyMetricRepository.findByKpiReport_IdAndDay(kpiReport.getId(), day);

        if (existingDailyMetric.isPresent()) {
            var dailyMetric = existingDailyMetric.get();
            log.info("Existing DailyMetric found with ID: {} for day: {}", dailyMetric.getId(), day);

            // Remove the association between DailyMetric and Metrics
            List<Metric> metrics = dailyMetric.getMetrics();
            for (Metric metric : metrics) {
                metric.setDailyMetric(null); // Break the association
            }

            dailyMetric.getMetrics().clear(); // Clear the list
            dailyMetricRepository.save(dailyMetric); // Save the DailyMetric

            // Now delete the metrics
            metricRepository.deleteAll(metrics);

            return dailyMetric;
        } else {
            log.info("No existing DailyMetric found. Creating new DailyMetric for day: {}", day);
            return dailyMetricRepository.save(DailyMetric.builder()
                .kpiReport(kpiReport)
                .day(day)
                .build());
        }
    }

    public void saveMetrics(List<Metric> metrics) {
        metricRepository.saveAll(metrics);
    }
}
