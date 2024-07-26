package com.blc.kpiReport.models.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GenerateKpiReportByLocationRequest {
    private int month;
    private int year;
    private String ghlLocationId;
}
