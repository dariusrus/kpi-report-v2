package com.blc.kpiReport.models.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class InformationDto {
    private String sessionsCount;

    @JsonProperty("sessionsWithMetricPercentage")
    private Double sessionsWithMetricPercentage;

    @JsonProperty("sessionsWithoutMetricPercentage")
    private Double sessionsWithoutMetricPercentage;

    private String pagesViews;
    private String subTotal;

    @JsonProperty("Device")
    private String device;

    @JsonProperty("Channel")
    private String channel;

    @JsonProperty("Source")
    private String source;

    @JsonProperty("totalTime")
    private Integer totalTime;

    @JsonProperty("activeTime")
    private Integer activeTime;

    @JsonProperty("averageScrollDepth")
    private Double averageScrollDepth;

    @JsonProperty("totalSessionCount")
    private String totalSessionCount;

    @JsonProperty("totalBotSessionCount")
    private String totalBotSessionCount;

    @JsonProperty("distinctUserCount")
    private String distinctUserCount;

    @JsonProperty("pagesPerSessionPercentage")
    private Double pagesPerSessionPercentage;

    @JsonProperty("Url")
    private String url;
}
