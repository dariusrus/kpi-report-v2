package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class NewLeadAppointment {
    private int scheduledACallCount;
    private int noScheduledCallsCount;
}