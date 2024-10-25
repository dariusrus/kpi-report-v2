package com.blc.kpiReport.service.ga;

import com.blc.kpiReport.schema.ga.CityAnalytics;
import com.google.api.services.analyticsdata.v1beta.model.Row;
import com.google.api.services.analyticsdata.v1beta.model.RunReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GaDataProcessorService {

    public List<CityAnalytics> processCityAnalytics(RunReportResponse rawCityAnalytics) {
        log.debug("Starting process of city analytics data from the Google Analytics report.");

        var cityAnalytics = extractCityAnalytics(rawCityAnalytics);
        log.debug("Extracted {} city analytics entries from the raw report.", cityAnalytics.size());

        var filteredCityAnalytics = filterCityAnalytics(cityAnalytics);
        log.info("Filtered city analytics data. {} entries remaining after applying filters.", filteredCityAnalytics.size());

        return filteredCityAnalytics;
    }

    private List<CityAnalytics> filterCityAnalytics(List<CityAnalytics> cityAnalytics) {
        log.debug("Filtering city analytics based on average session duration threshold (>= 30 seconds).");

        var filteredCityAnalytics = new ArrayList<CityAnalytics>();
        for (var cityAnalytic : cityAnalytics) {
            if (cityAnalytic.getAverageSessionDuration() >= 30 && cityAnalytic.getUniqueSiteVisitors() > 0) {
                filteredCityAnalytics.add(cityAnalytic);
                log.debug("City {} passed the filter with average session duration of {} seconds.",
                        cityAnalytic.getCity(), cityAnalytic.getAverageSessionDuration());
            } else {
                log.debug("City {} did not pass the filter with average session duration of {} seconds.",
                        cityAnalytic.getCity(), cityAnalytic.getAverageSessionDuration());
            }
        }
        log.info("Completed filtering process. {} entries passed the filter.", filteredCityAnalytics.size());
        return filteredCityAnalytics;
    }

    private List<CityAnalytics> extractCityAnalytics(RunReportResponse response) {
        log.debug("Extracting city analytics from the response.");

        List<CityAnalytics> countryAnalyticsList = new ArrayList<>();
        if (response.getRows() != null && !response.getRows().isEmpty()) {
            log.debug("Response contains {} rows.", response.getRows().size());

            for (Row row : response.getRows()) {
                var city = row.getDimensionValues().get(0).getValue();
                int uniqueSiteVisitors = Integer.parseInt(row.getMetricValues().get(0).getValue());
                double averageSessionDuration = Double.parseDouble(row.getMetricValues().get(1).getValue());

                log.debug("Processing city: {}, unique site visitors: {}, average session duration: {} seconds.",
                        city, uniqueSiteVisitors, averageSessionDuration);

                var cityAnalytics = CityAnalytics.builder()
                        .city(city)
                        .uniqueSiteVisitors(uniqueSiteVisitors)
                        .averageSessionDuration(averageSessionDuration)
                        .build();

                countryAnalyticsList.add(cityAnalytics);
            }
            log.debug("Extracted analytics for {} cities.", countryAnalyticsList.size());
        } else {
            log.warn("Response contains no rows. No city analytics to process.");
        }

        return countryAnalyticsList;
    }

    public Integer aggregateUniqueSiteVisitors(List<CityAnalytics> cityAnalyticsList) {
        log.debug("Aggregating total unique site visitors from {} city analytics entries.", cityAnalyticsList.size());

        int totalUniqueSiteVisitors = cityAnalyticsList.stream()
                .mapToInt(CityAnalytics::getUniqueSiteVisitors)
                .sum();

        log.info("Total unique site visitors aggregated: {}", totalUniqueSiteVisitors);
        return totalUniqueSiteVisitors;
    }
}