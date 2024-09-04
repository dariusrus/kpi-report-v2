package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.request.GenerateKpiReportsRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportBatchResponse;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.service.KpiReportGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "KPI Report Generator API", description = "Endpoints used to generate and manage KPI Reports.")
public class KpiReportGeneratorController {

    private final KpiReportGeneratorService generatorService;

    @Operation(
        summary = "Generate a KPI Report for a specific GHL Location ID.",
        description = "Generate a monthly KPI Report by connecting to Google Analytics API and GoHighLevel API.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Report generated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateKpiReportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping(path="/monthly" , produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<GenerateKpiReportResponse> generateKpiReport(@RequestBody GenerateKpiReportByLocationRequest request) {
        return generatorService.generateKpiReportByLocation(request);
    }

    @Operation(
        summary = "Generate KPI Reports for all configured locations.",
        description = "Generate monthly KPI Reports for all configured GHL locations.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reports generated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateKpiReportResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/monthly/batch")
    public CompletableFuture<List<GenerateKpiReportResponse>> generateAllKpiReports(@RequestBody GenerateKpiReportsRequest request) throws IOException {
        return generatorService.generateAllKpiReports(request, false);
    }

    @Operation(
        summary = "Generate the average Opportunity-to-Lead for a specific month and year.",
        description = "Calculate the average Opportunity-to-Lead from all existing reports for a specified month and year.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Average Opportunity-to-Lead has been generated successfully"
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping(path="/monthly/average", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> generateAverageOpportunityToLead(@RequestBody GenerateKpiReportsRequest request) {
        generatorService.calculateAverageOpportunityToLead(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Fetch KPI Report Generation Status by ID.",
        description = "Retrieve the KPI Report status and details by report ID.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Report fetched successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateKpiReportResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/{id}/status")
    public GenerateKpiReportResponse getKpiReportStatus(@PathVariable Long id) {
        return generatorService.getKpiReportStatus(id);
    }

    @Operation(
        summary = "Fetch KPI Report Generation statuses for a specific year and month batch.",
        description = "Retrieve the KPI Reports status for a specified year and month.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Reports fetched successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenerateKpiReportBatchResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reports not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/batch/status")
    public GenerateKpiReportBatchResponse getKpiReportStatusByYearAndMonth(@RequestParam int month, @RequestParam int year) {
        return generatorService.getKpiReportStatusByMonthAndYear(month, year);
    }
}
