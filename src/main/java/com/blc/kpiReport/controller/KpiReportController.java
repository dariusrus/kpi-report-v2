package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.response.KpiReportResponse;
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
public class KpiReportController {

    private final KpiReportRetrievalService retrievalService;

    @Operation(
        summary = "Fetch KPI Report by month, year, and location ID.",
        description = "Retrieve the KPI Report for a specified location, year, and month.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Report fetched successfully",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping()
    public KpiReportResponse getReportByMonthYearAndLocation(@RequestParam String ghlLocationId, @RequestParam int month, @RequestParam int year) {
        return retrievalService.getKpiReport(ghlLocationId, month, year);
    }
}
