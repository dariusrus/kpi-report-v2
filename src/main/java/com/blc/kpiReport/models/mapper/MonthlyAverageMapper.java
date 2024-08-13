package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.MonthlyAverageResponse;
import com.blc.kpiReport.schema.MonthlyAverage;
import org.springframework.stereotype.Component;

import static com.blc.kpiReport.util.DateUtil.formatMonthAndYear;
import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class MonthlyAverageMapper {

    public MonthlyAverageResponse toResponse(MonthlyAverage monthlyAverage) {
        return MonthlyAverageResponse.builder()
            .monthAndYear(formatMonthAndYear(monthlyAverage.getMonth(), monthlyAverage.getYear()))
            .clientType(monthlyAverage.getClientType().toString())
            .averageUniqueSiteVisitors(Math.round(monthlyAverage.getAverageUniqueSiteVisitors()))
            .averageTotalLeads(Math.round(monthlyAverage.getAverageTotalLeads()))
            .averageOpportunityToLead(roundToTwoDecimalPlaces(monthlyAverage.getAverageOpportunityToLead()))
            .weightedAverageOpportunityToLead(roundToTwoDecimalPlaces(monthlyAverage.getWeightedAverageOpportunityToLead()))
            .build();
    }
}