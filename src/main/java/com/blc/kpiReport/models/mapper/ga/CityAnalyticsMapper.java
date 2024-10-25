package com.blc.kpiReport.models.mapper.ga;

import com.blc.kpiReport.models.response.ga.CityAnalyticsResponse;
import com.blc.kpiReport.models.response.ghl.AppointmentResponse;
import com.blc.kpiReport.schema.ga.CityAnalytics;
import com.blc.kpiReport.schema.ghl.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class CityAnalyticsMapper {

    public CityAnalyticsResponse toResponse(CityAnalytics cityAnalytics) {
        return CityAnalyticsResponse.builder()
            .city(cityAnalytics.getCity())
            .uniqueSiteVisitors(cityAnalytics.getUniqueSiteVisitors())
            .averageSessionDuration(cityAnalytics.getAverageSessionDuration())
            .build();
    }

    public List<CityAnalyticsResponse> toResponseList(List<CityAnalytics> cityAnalytics) {
        return cityAnalytics.stream()
            .map(this::toResponse)
            .sorted((c1, c2) -> Integer.compare(c2.getUniqueSiteVisitors(), c1.getUniqueSiteVisitors()))
            .collect(Collectors.toList());
    }
}
