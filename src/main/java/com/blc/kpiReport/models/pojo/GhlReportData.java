package com.blc.kpiReport.models.pojo;

import com.blc.kpiReport.schema.ghl.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GhlReportData {
    private List<LeadSource> leadSources;
    private List<Calendar> calendars;
    private List<PipelineStage> pipelineStages;
    private List<ContactWon> contactsWon;
    private List<SalesPersonConversation> salesPersonConversations;
    private List<FollowUpConversion> followUpConversions;
    private List<ContactScheduledAppointment> contactScheduledAppointments;
}

