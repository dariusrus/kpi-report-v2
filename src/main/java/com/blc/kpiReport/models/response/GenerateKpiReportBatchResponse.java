package com.blc.kpiReport.models.response;

import com.blc.kpiReport.models.ReportStatus;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GenerateKpiReportBatchResponse {
    private String monthAndYear;
    private ReportStatus status;
    private double percentageDone;
    private String successRatio;
    private List<String> failedReports;
    private String totalTimeElapsed;
    private List<GenerateKpiReportResponse> kpiReports;
}

