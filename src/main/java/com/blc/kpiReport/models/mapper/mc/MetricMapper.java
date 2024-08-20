package com.blc.kpiReport.models.mapper.mc;

import com.blc.kpiReport.models.mapper.ghl.InformationMapper;
import com.blc.kpiReport.models.response.mc.MetricResponse;
import com.blc.kpiReport.schema.mc.Metric;
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
