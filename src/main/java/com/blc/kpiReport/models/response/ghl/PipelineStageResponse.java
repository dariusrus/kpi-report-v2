package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PipelineStageResponse {
    private String stageName;
    private int count;
    private double percentage;
    private double monetaryValue;
    private List<SalesPersonConversionResponse> salesPersonConversions;
}

