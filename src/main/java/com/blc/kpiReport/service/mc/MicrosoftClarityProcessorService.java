package com.blc.kpiReport.service.mc;

import com.blc.kpiReport.models.pojo.InformationDto;
import com.blc.kpiReport.models.pojo.MetricDto;
import com.blc.kpiReport.schema.Information;
import com.blc.kpiReport.schema.Metric;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MicrosoftClarityProcessorService {

    private final ObjectMapper objectMapper;

    public List<Metric> processMetrics(String dailyMetrics) throws JsonProcessingException {
        // Deserialize JSON into MetricDto list
        List<MetricDto> metricDTOs = objectMapper.readValue(dailyMetrics, new TypeReference<List<MetricDto>>() {});

        // List to hold Metric entities
        List<Metric> metrics = new ArrayList<>();

        for (MetricDto metricDto : metricDTOs) {
            Metric metric = Metric.builder()
                .metricName(Optional.ofNullable(metricDto.getMetricName()).orElse(null))
                .informationList(new ArrayList<>()) // Will add Information later
                .build();

            for (InformationDto infoDto : metricDto.getInformation()) {
                Information information = Information.builder()
                    .sessionsCount(parseInteger(infoDto.getSessionsCount()))
                    .sessionsWithMetricPercentage(parseDouble(infoDto.getSessionsWithMetricPercentage()))
                    .sessionsWithoutMetricPercentage(parseDouble(infoDto.getSessionsWithoutMetricPercentage()))
                    .pagesViews(parseInteger(infoDto.getPagesViews()))
                    .subTotal(parseInteger(infoDto.getSubTotal()))
                    .totalTime(infoDto.getTotalTime())
                    .activeTime(infoDto.getActiveTime())
                    .averageScrollDepth(infoDto.getAverageScrollDepth())
                    .totalSessionCount(infoDto.getTotalSessionCount())
                    .totalBotSessionCount(infoDto.getTotalBotSessionCount())
                    .distinctUserCount(infoDto.getDistinctUserCount())
                    .pagesPerSessionPercentage(infoDto.getPagesPerSessionPercentage())
                    .device(infoDto.getDevice())
                    .channel(infoDto.getChannel())
                    .source(infoDto.getSource())
                    .metric(metric)
                    .build();
                metric.getInformationList().add(information);
            }
            metrics.add(metric);
        }
        return metrics;
    }

    private Integer parseInteger(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Double value) {
        return value; // Directly return the value; it can be null
    }

    private Double parseDouble(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}