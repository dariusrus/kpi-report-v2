package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
class WebsiteAnalytics {
    private List<DeviceClarityAggregate> deviceClarityAggregate;
    private List<TopTenUrl> topTenUrls;
}
