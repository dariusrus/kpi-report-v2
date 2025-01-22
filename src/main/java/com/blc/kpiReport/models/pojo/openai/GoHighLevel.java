package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class GoHighLevel {
    private int totalLeadsCaptured;
    private int industryAverageTotalLeadsCaptured;
    private List<PreviousMonthLeads> previousMonthTotalLeadsCaptured;
    private int totalWebsiteLeadsCaptured;
    private int totalManualUserInputLeadsCaptured;
    private List<TopLeadSource> topLeadSources;
    private List<TopSessionChannel> topSessionChannels;
    private NewLeadAppointment newLeadAppointment;
    private PipelineStageConversions pipelineStageConversions;
}