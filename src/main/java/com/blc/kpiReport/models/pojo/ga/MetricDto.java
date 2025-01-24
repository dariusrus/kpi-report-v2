package com.blc.kpiReport.models.pojo.ga;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class MetricDto {
    @JsonProperty("metricName")
    private String metricName;

    @JsonProperty("information")
    private List<InformationDto> information;
}
