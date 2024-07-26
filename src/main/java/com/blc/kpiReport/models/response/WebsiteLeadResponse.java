package com.blc.kpiReport.models.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WebsiteLeadResponse {
    private int totalLeads;
    private double totalValues;
    private int totalOpen;
    private int totalWon;
    private int totalLost;
    private int totalAbandoned;
    private List<LeadSourceResponse> leadSource;
}

