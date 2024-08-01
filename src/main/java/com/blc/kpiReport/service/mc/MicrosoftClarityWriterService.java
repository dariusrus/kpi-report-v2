package com.blc.kpiReport.service.mc;

import com.blc.kpiReport.repository.DailyMetricRepository;
import com.blc.kpiReport.repository.MetricRepository;
import com.blc.kpiReport.schema.DailyMetric;
import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.Metric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicrosoftClarityWriterService {
    private final DailyMetricRepository dailyMetricRepository;
    private final MetricRepository metricRepository;

    public DailyMetric getOrCreateDailyMetric(KpiReport kpiReport, int day) {
        var existingDailyMetric = dailyMetricRepository.findByKpiReport_IdAndDay(kpiReport.getId(), day);

        if (existingDailyMetric.isPresent()) {
            var dailyMetric = existingDailyMetric.get();
            log.info("Existing DailyMetric found with ID: {} for day: {}", dailyMetric.getId(), day);
            // Delete existing metrics associated with this DailyMetric
            metricRepository.deleteAll(dailyMetric.getMetrics());
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
