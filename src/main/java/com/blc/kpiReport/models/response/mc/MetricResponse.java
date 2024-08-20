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
public class MetricResponse {
    private Long id;
    private String metricName;
    private List<InformationResponse> informationList;
}
