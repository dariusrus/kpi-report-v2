package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.response.mc.DailyMetricResponse;
import com.blc.kpiReport.service.KpiReportRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "KPI Report Retrieval API", description = "Endpoints used to retrieve KPI Reports.")
public class MicrosoftAnalyticsController {

    private final KpiReportRetrievalService retrievalService;

    @Operation(
        summary = "Fetch Daily Metric by day, month, year, and location ID.",
        description = "Retrieve the Daily Metric for a specified location, day, month, and year.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Daily Metric fetched successfully",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Daily Metric not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/daily-metric")
    public DailyMetricResponse getDailyMetric(@RequestParam String ghlLocationId, @RequestParam int day, @RequestParam int month, @RequestParam int year) {
        return retrievalService.getDailyMetric(ghlLocationId, day, month, year);
    }
}
