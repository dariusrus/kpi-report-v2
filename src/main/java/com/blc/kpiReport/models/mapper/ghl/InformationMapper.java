package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.mc.InformationResponse;
import com.blc.kpiReport.schema.mc.Information;
import org.springframework.stereotype.Component;

@Component
public class InformationMapper {

    public InformationResponse toResponse(Information information) {
        return InformationResponse.builder()
            .id(information.getId())
            .sessionsCount(information.getSessionsCount())
            .sessionsWithMetricPercentage(information.getSessionsWithMetricPercentage())
            .sessionsWithoutMetricPercentage(information.getSessionsWithoutMetricPercentage())
            .pagesViews(information.getPagesViews())
            .subTotal(information.getSubTotal())
            .totalTime(information.getTotalTime())
            .activeTime(information.getActiveTime())
            .averageScrollDepth(information.getAverageScrollDepth())
            .totalSessionCount(information.getTotalSessionCount())
            .totalBotSessionCount(information.getTotalBotSessionCount())
            .distinctUserCount(information.getDistinctUserCount())
            .pagesPerSessionPercentage(information.getPagesPerSessionPercentage())
            .device(information.getDevice())
            .channel(information.getChannel())
            .source(information.getSource())
            .url(information.getUrl())
            .build();
    }
}
