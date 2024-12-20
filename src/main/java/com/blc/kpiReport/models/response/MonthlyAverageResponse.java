package com.blc.kpiReport.models.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MonthlyAverageResponse {
    private String monthAndYear;
    private String clientType;
    private int averageUniqueSiteVisitors;
    private int averageTotalLeads;
    private double averageOpportunityToLead;
    private double weightedAverageOpportunityToLead;
    private int averageFollowUps;
    private int averageConversions;
    private double averageFollowUpPerConversion;
    private int averageTotalFollowUps;
    private int averageTotalConversions;
    private double averageTotalFollowUpPerConversion;
}