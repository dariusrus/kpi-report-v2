package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PreviousMonthLeads {
    private String monthAndYear;
    private int totalLeadsCaptured;
}
