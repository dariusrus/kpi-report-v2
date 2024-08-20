package com.blc.kpiReport.models.response.mc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceMetricResponse {
    private Long id;
    private String deviceType;
    private Double averageScrollDepth;
    private Integer totalTime;
    private Integer activeTime;
    private Integer totalSessionCount;
}