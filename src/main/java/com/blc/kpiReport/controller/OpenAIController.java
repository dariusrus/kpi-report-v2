package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.pojo.openai.PromptTemplate;
import com.blc.kpiReport.service.openai.OpenAIGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/openai")
@Tag(name = "OpenAI API", description = "Endpoints for managing OpenAI prompts and generation.")
public class OpenAIController {

    private final OpenAIGeneratorService openAIGeneratorService;

    @Operation(
            summary = "Update the OpenAI prompt template",
            description = "Dynamically update the OpenAI prompt template used for generating executive summaries.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Prompt template updated successfully.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid prompt template provided."),
                    @ApiResponse(responseCode = "500", description = "Internal server error occurred.")
            }
    )
    @PostMapping("/modify-prompt")
    public PromptTemplate modifyPrompt(@RequestBody PromptTemplate updatedPrompt) {
        String updatedTemplate = openAIGeneratorService.updatePrompt(updatedPrompt.getTemplate());
        return new PromptTemplate(updatedTemplate);
    }

    @Operation(
            summary = "Retrieve the current OpenAI prompt template",
            description = "Fetch the current OpenAI prompt template being used for generating executive summaries.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Prompt template retrieved successfully.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "500", description = "Internal server error occurred.")
            }
    )
    @GetMapping("/get-prompt")
    public PromptTemplate getPrompt() {
        String currentTemplate = openAIGeneratorService.getPrompt();
        return new PromptTemplate(currentTemplate);
    }
}
