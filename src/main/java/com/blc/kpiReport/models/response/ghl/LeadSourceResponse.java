package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
    private String leadType;
    private List<LeadContactResponse> leadContacts;
}
