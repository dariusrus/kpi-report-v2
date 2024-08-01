package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.DailyMetricResponse;
import com.blc.kpiReport.schema.DailyMetric;
import com.blc.kpiReport.schema.GhlLocation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class DailyMetricMapper {

    private final MetricMapper metricMapper;

    public DailyMetricMapper(MetricMapper metricMapper) {
        this.metricMapper = metricMapper;
    }

    public DailyMetricResponse toResponse(DailyMetric dailyMetric, GhlLocation ghlLocation) {
        return DailyMetricResponse.builder()
            .subAgency(ghlLocation.getName()) // Assuming KpiReport has a subAgency field
            .ghlLocationId(ghlLocation.getLocationId())
            .date(formatDate(dailyMetric)) // Format date as "January 1, 2024"
            .metric(dailyMetric.getMetrics().stream()
                .map(metricMapper::toResponse)
                .collect(Collectors.toList()))
            .build();
    }

    private String formatDate(DailyMetric dailyMetric) {
        LocalDate date = LocalDate.of(dailyMetric.getKpiReport().getYear(), dailyMetric.getKpiReport().getMonth(), dailyMetric.getDay());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return date.format(formatter);
    }
}
