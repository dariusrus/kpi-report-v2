package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.models.response.KpiReportResponse;
import com.blc.kpiReport.models.response.MonthlyAverageResponse;
import com.blc.kpiReport.service.KpiReportRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
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

    @Operation(
        summary = "Fetch Monthly Average by month, year, and client type.",
        description = "Retrieve the Monthly Average for a specified month, year, and client type.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Monthly average fetched successfully",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Monthly average not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/monthly/average")
    public MonthlyAverageResponse getMonthlyAverage(
        @RequestParam int month,
        @RequestParam int year,
        @RequestParam @Parameter(description = "Client type", required = true, example = "REMODELING") ClientType clientType) {
        return retrievalService.getMonthlyAverage(month, year, clientType);
    }
}
