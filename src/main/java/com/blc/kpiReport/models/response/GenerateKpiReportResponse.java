package com.blc.kpiReport.models.response;

import com.blc.kpiReport.models.ReportStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GenerateKpiReportResponse {
    private Long id;
    private String subAgency;
    private String ghlLocationId;
    private ReportStatus status;
    private String timeElapsed;
}

