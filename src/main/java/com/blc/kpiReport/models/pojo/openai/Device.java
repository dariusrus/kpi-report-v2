package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
class Device {
    private String deviceType;
    private double averageScrollDepth;
    private int totalTime;
    private int activeTime;
    private int totalSessionCount;
}
