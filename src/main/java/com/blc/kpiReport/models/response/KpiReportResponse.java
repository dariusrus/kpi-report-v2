package com.blc.kpiReport.models.response;

import com.blc.kpiReport.models.response.ga.CityAnalyticsResponse;
import com.blc.kpiReport.models.response.ghl.*;
import com.blc.kpiReport.models.response.mc.MonthlyClarityReportResponse;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KpiReportResponse {
    private String subAgency;
    private String ghlLocationId;
    private String country;
    private String monthAndYear;
    private String clientType;
    private int uniqueSiteVisitors;
    private List<CityAnalyticsResponse> cityAnalytics;
    private double opportunityToLead;
    private WebsiteLeadResponse websiteLead;
    private List<CalendarResponse> calendars;
    private List<PipelineResponse> pipelines;
    private List<ContactsWonResponse> contactsWon;
    private List<SalesPersonConversationResponse> salesPersonConversations;
    private MonthlyClarityReportResponse monthlyClarityReport;
    private List<GhlUserResponse> ghlUsers;
}

