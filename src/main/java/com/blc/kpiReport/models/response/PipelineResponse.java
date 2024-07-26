package com.blc.kpiReport.models.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PipelineResponse {
    private String pipelineName;
    private int totalCount;
    private List<PipelineStageResponse> pipelineStages;
}

