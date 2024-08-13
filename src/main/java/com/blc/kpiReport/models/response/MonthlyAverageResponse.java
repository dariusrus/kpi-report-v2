package com.blc.kpiReport.models.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MonthlyAverageResponse {
    private String monthAndYear;
    private int averageUniqueSiteVisitors;
    private int averageTotalLeads;
    private double averageOpportunityToLead;
    private double weightedAverageOpportunityToLead;
}