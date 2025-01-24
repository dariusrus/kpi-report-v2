package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SalesPersonBreakdown {
    private String salesPersonName;
    private int stageConversions;
    private int followUps;
}
