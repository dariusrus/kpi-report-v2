package com.blc.kpiReport.models.response.ga;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CityAnalyticsResponse {
    private String city;
    private int uniqueSiteVisitors;
    private double averageSessionDuration;
}

