package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.MonthlyAverageResponse;
import com.blc.kpiReport.schema.MonthlyAverage;
import org.springframework.stereotype.Component;

import static com.blc.kpiReport.util.DateUtil.formatMonthAndYear;
import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class MonthlyAverageMapper {

    public MonthlyAverageResponse toResponse(MonthlyAverage monthlyAverage) {
        if (monthlyAverage == null) {
            return MonthlyAverageResponse.builder().build();
        }

        return MonthlyAverageResponse.builder()
                .monthAndYear(formatMonthAndYear(
                        defaultIfNull(monthlyAverage.getMonth(), 0),
                        defaultIfNull(monthlyAverage.getYear(), 0)
                ))
                .clientType(monthlyAverage.getClientType() != null
                        ? monthlyAverage.getClientType().toString()
                        : "UNKNOWN")
                .averageUniqueSiteVisitors(Math.round(defaultIfNull(monthlyAverage.getAverageUniqueSiteVisitors(), 0)))
                .averageTotalLeads(Math.round(defaultIfNull(monthlyAverage.getAverageTotalLeads(), 0)))
                .averageOpportunityToLead(roundToTwoDecimalPlaces(
                        defaultIfNull(monthlyAverage.getAverageOpportunityToLead(), 0.0)))
                .weightedAverageOpportunityToLead(roundToTwoDecimalPlaces(
                        defaultIfNull(monthlyAverage.getWeightedAverageOpportunityToLead(), 0.0)))
                .averageSms(defaultIfNull(monthlyAverage.getAverageSms(), 0))
                .averageCalls(defaultIfNull(monthlyAverage.getAverageCalls(), 0))
                .averageEmails(defaultIfNull(monthlyAverage.getAverageEmails(), 0))
                .averageLiveChatMessages(defaultIfNull(monthlyAverage.getAverageLiveChatMessage(), 0))
                .averageTotalFollowUps(defaultIfNull(monthlyAverage.getAverageTotalFollowUps(), 0))
                .averageTotalConversions(defaultIfNull(monthlyAverage.getAverageTotalConversions(), 0))
                .averageTotalFollowUpPerConversion(roundToTwoDecimalPlaces(defaultIfNull(monthlyAverage.getAverageTotalFollowUpPerConversion(), 0.0)))
                .build();
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}