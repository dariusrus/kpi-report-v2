package com.blc.kpiReport.models.pojo.openai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents the OpenAI prompt template.")
public class PromptTemplate {

    @Schema(description = "The content of the OpenAI prompt template.", example = "Your updated prompt here.")
    private String template;
}
