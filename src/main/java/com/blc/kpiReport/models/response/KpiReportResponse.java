package com.blc.kpiReport.models.response;

import com.blc.kpiReport.models.response.ghl.AppointmentResponse;
import com.blc.kpiReport.models.response.ghl.ContactsWonResponse;
import com.blc.kpiReport.models.response.ghl.PipelineResponse;
import com.blc.kpiReport.models.response.ghl.WebsiteLeadResponse;
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
    private String monthAndYear;
    private String clientType;
    private int uniqueSiteVisitors;
    private double opportunityToLead;
    private WebsiteLeadResponse websiteLead;
    private List<AppointmentResponse> appointments;
    private List<PipelineResponse> pipelines;
    private List<ContactsWonResponse> contactsWon;
    private MonthlyClarityReportResponse monthlyClarityReport;
}

