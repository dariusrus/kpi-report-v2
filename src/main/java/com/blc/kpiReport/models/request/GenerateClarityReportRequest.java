package com.blc.kpiReport.models.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GenerateClarityReportRequest {
    private String ghlLocationId;
}
