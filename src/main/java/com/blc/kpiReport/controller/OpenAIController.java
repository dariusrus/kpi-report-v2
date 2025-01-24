package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.response.KpiReportResponse;
import com.blc.kpiReport.service.openai.OpenAIGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
@Tag(name = "KPI Report Retrieval API", description = "Endpoints used to retrieve KPI Reports.")
public class OpenAIController {

    private final OpenAIGeneratorService openAIGeneratorService;

    @Operation(
            summary = "Fetch generated executive summary by location ID",
            description = "Retrieve the OpenAI generated summary for a specified location, month, and year.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Daily Metric fetched successfully",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "404", description = "Report not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/openai")
    public KpiReportResponse getReportByMonthYearAndLocation(@RequestParam String ghlLocationId, @RequestParam int month, @RequestParam int year) throws IOException {
//        openAIGeneratorService.chatGPTRequest("test");
        return null;
    }
}
