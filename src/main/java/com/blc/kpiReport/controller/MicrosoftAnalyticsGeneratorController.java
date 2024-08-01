package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.request.GenerateClarityReportRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.service.KpiReportGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "KPI Report Generator API", description = "Endpoints used to generate and manage KPI Reports.")
public class MicrosoftAnalyticsGeneratorController {

    private final KpiReportGeneratorService generatorService;

    @Operation(
        summary = "Generate a daily Microsoft Analytics Report for a specific GHL Location ID.",
        description = "Generate the daily report from Microsoft Analytics.",
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
    @PostMapping(path="/daily" , produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<GenerateKpiReportResponse> generateMicrosoftAnalyticsReport(@RequestBody GenerateClarityReportRequest request) throws IOException {
        return generatorService.generateDailyMicrosoftAnalyticsReport(request);
    }
}
