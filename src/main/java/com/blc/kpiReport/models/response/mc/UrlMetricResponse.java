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
public class UrlMetricResponse {
    private Long id;
    private String url;
    private List<DeviceMetricResponse> devices;
}