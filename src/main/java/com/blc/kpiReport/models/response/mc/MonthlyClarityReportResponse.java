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
public class MonthlyClarityReportResponse {
    private Long id;
    private List<DeviceClarityAggregateResponse> deviceClarityAggregate;
    private List<UrlMetricResponse> urls;
}