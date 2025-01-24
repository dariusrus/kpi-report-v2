package com.blc.kpiReport.models.pojo.ghl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PipelineStageInfo {
    private String stageName;
    private String pipelineName;
    private int position;
}

