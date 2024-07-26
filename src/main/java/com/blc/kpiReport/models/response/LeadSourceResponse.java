package com.blc.kpiReport.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LeadSourceResponse {
    private String source;
    private int totalLeads;
    private double totalValues;
    private int open;
    private int won;
    private int lost;
    private int abandoned;
    private double winPercentage;
}
