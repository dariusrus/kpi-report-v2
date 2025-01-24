package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.request.GenerateKpiReportsRequest;
import com.blc.kpiReport.service.openai.OpenAIGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "KPI Report Generator API", description = "Endpoints used to generate and manage KPI Reports.")
public class OpenAIGeneratorController {

    private final OpenAIGeneratorService openAIGeneratorService;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Operation(
            summary = "Generate a monthly executive summary for a specific GHL Location ID.",
            description = "Generate the monthly AI generated summary using OpenAI.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report generated successfully"
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping(path="/openai/summary")
    public String generateOpenAISummary(@RequestBody GenerateKpiReportByLocationRequest request) {
        return openAIGeneratorService.generateExecutiveSummary(request.getGhlLocationId(), request.getMonth(), request.getYear());
    }

    @Operation(
            summary = "Generate monthly executive summaries for configured GHL Location IDs.",
            description = "Trigger AI-generated executive summaries for all GHL locations configured in the application properties. This batch process runs in the background using the location IDs specified in the configuration file.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Batch report generation triggered successfully"
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping(path = "/openai/summary/batch")
    public ResponseEntity<String> generateOpenAISummaryBatch(@RequestBody GenerateKpiReportsRequest request) {
        taskExecutor.execute(() -> openAIGeneratorService.generateExecutiveSummaryBatch(request.getMonth(), request.getYear()));
        return ResponseEntity.ok("OpenAI Executive Summary generation executed.");
    }
}
