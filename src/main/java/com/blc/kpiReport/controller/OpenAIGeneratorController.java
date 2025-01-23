package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.service.openai.OpenAIGeneratorService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "KPI Report Generator API", description = "Endpoints used to generate and manage KPI Reports.")
public class OpenAIGeneratorController {

    private final OpenAIGeneratorService openAIGeneratorService;

    @Operation(
            summary = "Generate a monthly executive summary for a specific GHL Location ID.",
            description = "Generate the monthly AI generated summary using OpenAI.",
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
    @PostMapping(path="/openai/summary" , produces = MediaType.APPLICATION_JSON_VALUE)
    public String generateOpenAISummary(@RequestBody GenerateKpiReportByLocationRequest request) {
        return openAIGeneratorService.generateExecutiveSummary(request);
    }
}
