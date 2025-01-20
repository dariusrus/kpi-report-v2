package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
class GoogleAnalytics {
    private int totalUniqueSiteVisitors;
    private int industryAverageUniqueSiteVisitors;
    private List<PreviousMonthVisitors> previousMonthsTotalUniqueSiteVisitors;
    private List<TopCity> topCities;
}
