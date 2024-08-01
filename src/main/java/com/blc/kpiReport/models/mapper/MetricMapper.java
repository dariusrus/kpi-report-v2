package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.MetricResponse;
import com.blc.kpiReport.schema.Metric;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MetricMapper {

    private final InformationMapper informationMapper;

    public MetricMapper(InformationMapper informationMapper) {
        this.informationMapper = informationMapper;
    }

    public MetricResponse toResponse(Metric metric) {
        return MetricResponse.builder()
            .id(metric.getId())
            .metricName(metric.getMetricName())
            .informationList(metric.getInformationList().stream()
                .map(informationMapper::toResponse)
                .collect(Collectors.toList()))
            .build();
    }
}
