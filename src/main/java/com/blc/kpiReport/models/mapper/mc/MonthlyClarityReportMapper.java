package com.blc.kpiReport.models.mapper.mc;

import com.blc.kpiReport.models.response.mc.DeviceClarityAggregateResponse;
import com.blc.kpiReport.models.response.mc.DeviceMetricResponse;
import com.blc.kpiReport.models.response.mc.MonthlyClarityReportResponse;
import com.blc.kpiReport.models.response.mc.UrlMetricResponse;
import com.blc.kpiReport.schema.mc.DeviceMetric;
import com.blc.kpiReport.schema.mc.MonthlyClarityReport;
import com.blc.kpiReport.schema.mc.UrlMetric;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class MonthlyClarityReportMapper {

    public MonthlyClarityReportResponse toResponse(MonthlyClarityReport monthlyClarityReport) {
        return MonthlyClarityReportResponse.builder()
            .id(monthlyClarityReport.getId())
            .urls(monthlyClarityReport.getUrls().stream()
                .map(this::toUrlMetricResponse)
                .collect(Collectors.toList()))
            .build();
    }

    private UrlMetricResponse toUrlMetricResponse(UrlMetric urlMetric) {
        return UrlMetricResponse.builder()
            .id(urlMetric.getId())
            .url(urlMetric.getUrl())
            .devices(urlMetric.getDevices().stream()
                .map(this::toDeviceMetricResponse)
                .collect(Collectors.toList()))
            .build();
    }

    private DeviceMetricResponse toDeviceMetricResponse(DeviceMetric deviceMetric) {
        return DeviceMetricResponse.builder()
            .id(deviceMetric.getId())
            .deviceType(deviceMetric.getDeviceType())
            .averageScrollDepth(roundToTwoDecimalPlaces(deviceMetric.getAverageScrollDepth()))
            .totalTime(deviceMetric.getTotalTime())
            .activeTime(deviceMetric.getActiveTime())
            .totalSessionCount(deviceMetric.getTotalSessionCount())
            .build();
    }

    public List<DeviceClarityAggregateResponse> aggregateDataByDeviceType(MonthlyClarityReport monthlyClarityReport) {
        Map<String, DeviceClarityAggregateResponse> deviceDataMap = new HashMap<>();
        Map<String, Integer> validScrollDepthEntriesMap = new HashMap<>();

        if (ObjectUtils.isNotEmpty(monthlyClarityReport) && !CollectionUtils.isEmpty(monthlyClarityReport.getUrls())) {
            for (UrlMetric urlMetric : monthlyClarityReport.getUrls()) {
                for (DeviceMetric deviceMetric : urlMetric.getDevices()) {
                    String deviceName = deviceMetric.getDeviceType();
                    deviceDataMap.putIfAbsent(deviceName, DeviceClarityAggregateResponse.builder()
                        .deviceName(deviceName)
                        .collectiveAverageScrollDepth(0.0)
                        .totalSessions(0)
                        .totalActiveTime(0)
                        .build());

                    DeviceClarityAggregateResponse aggregateResponse = deviceDataMap.get(deviceName);

                    if (deviceMetric.getTotalSessionCount() != null) {
                        aggregateResponse.setTotalSessions(aggregateResponse.getTotalSessions() + deviceMetric.getTotalSessionCount());
                    }

                    if (deviceMetric.getActiveTime() != null) {
                        aggregateResponse.setTotalActiveTime(aggregateResponse.getTotalActiveTime() + deviceMetric.getActiveTime());
                    }

                    if (deviceMetric.getAverageScrollDepth() != null) {
                        double updatedScrollDepth = aggregateResponse.getCollectiveAverageScrollDepth()
                            + deviceMetric.getAverageScrollDepth();
                        aggregateResponse.setCollectiveAverageScrollDepth(updatedScrollDepth);
                        validScrollDepthEntriesMap.put(deviceName, validScrollDepthEntriesMap.getOrDefault(deviceName, 0) + 1);
                    }
                }
            }

            for (Map.Entry<String, DeviceClarityAggregateResponse> entry : deviceDataMap.entrySet()) {
                String deviceName = entry.getKey();
                DeviceClarityAggregateResponse response = entry.getValue();
                int validScrollDepthEntries = validScrollDepthEntriesMap.getOrDefault(deviceName, 0);

                if (response.getCollectiveAverageScrollDepth() > 0 && validScrollDepthEntries > 0) {
                    var collectiveAverageScrollDepth = response.getCollectiveAverageScrollDepth() / validScrollDepthEntries;
                    response.setCollectiveAverageScrollDepth(roundToTwoDecimalPlaces(collectiveAverageScrollDepth));
                } else {
                    response.setCollectiveAverageScrollDepth(null);
                }
            }
            return new ArrayList<>(deviceDataMap.values());
        }
        return new ArrayList<>();
    }
}