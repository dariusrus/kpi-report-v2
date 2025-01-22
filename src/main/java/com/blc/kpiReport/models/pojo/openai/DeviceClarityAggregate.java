package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DeviceClarityAggregate {
    private String deviceName;
    private double collectiveAverageScrollDepth;
    private int totalSessions;
    private int totalActiveTime;
}