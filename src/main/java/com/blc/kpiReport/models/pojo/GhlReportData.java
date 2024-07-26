package com.blc.kpiReport.models.pojo;

import com.blc.kpiReport.schema.Appointment;
import com.blc.kpiReport.schema.ContactWon;
import com.blc.kpiReport.schema.LeadSource;
import com.blc.kpiReport.schema.PipelineStage;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GhlReportData {
    private List<LeadSource> leadSources;
    private List<Appointment> appointments;
    private List<PipelineStage> pipelineStages;
    private List<ContactWon> contactsWon;
}

