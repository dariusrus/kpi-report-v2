package com.blc.kpiReport.models.pojo.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class TopTenUrl {
    private String url;
    private List<Device> devices;
    private double averageScrollDepth;
    private int activeTime;
    private int totalSessionCount;
}
