package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WebsiteLeadResponse {
    private int totalLeads;
    private int totalWebsiteLeads;
    private int totalManualLeads;
    private double totalValues;
    private double totalWebsiteValuation;
    private double totalManualValuation;
    private int totalOpen;
    private int totalWon;
    private int totalLost;
    private int totalAbandoned;
    private List<LeadSourceResponse> leadSource;
}

