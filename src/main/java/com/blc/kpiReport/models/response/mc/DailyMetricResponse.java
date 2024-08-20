package com.blc.kpiReport.models.response.mc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyMetricResponse {
    private String subAgency;
    private String ghlLocationId;
    private String date;
    private List<MetricResponse> metric;
}
