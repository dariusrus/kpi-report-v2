package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
class PipelineStageConversions {
    private int totalStageConversions;
    private int totalFollowups;
    private List<SalesPersonBreakdown> salesPersonBreakdown;
}
