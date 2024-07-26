package com.blc.kpiReport.models.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenerateKpiReportsRequest {
    private int month;
    private int year;
}
