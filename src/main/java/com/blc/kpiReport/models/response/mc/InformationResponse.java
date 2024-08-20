package com.blc.kpiReport.models.response.mc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InformationResponse {
    private Long id;
    private Integer sessionsCount;
    private Double sessionsWithMetricPercentage;
    private Double sessionsWithoutMetricPercentage;
    private Integer pagesViews;
    private Integer subTotal;
    private Integer totalTime;
    private Integer activeTime;
    private Double averageScrollDepth;
    private String totalSessionCount;
    private String totalBotSessionCount;
    private String distinctUserCount;
    private Double pagesPerSessionPercentage;
    private String device;
    private String channel;
    private String source;
    private String url;
}
