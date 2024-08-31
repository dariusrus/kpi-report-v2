package com.blc.kpiReport.models.pojo;

import com.blc.kpiReport.schema.ghl.Calendar;
import com.blc.kpiReport.schema.ghl.ContactWon;
import com.blc.kpiReport.schema.ghl.LeadSource;
import com.blc.kpiReport.schema.ghl.PipelineStage;
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
}

