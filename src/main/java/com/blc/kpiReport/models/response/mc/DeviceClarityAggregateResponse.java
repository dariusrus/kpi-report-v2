package com.blc.kpiReport.models.response.mc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceClarityAggregateResponse {
    private String deviceName;
    private Double collectiveAverageScrollDepth;
    private Integer totalSessions;
    private Integer totalActiveTime;
}
