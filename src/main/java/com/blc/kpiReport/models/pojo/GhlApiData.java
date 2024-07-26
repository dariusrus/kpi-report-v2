package com.blc.kpiReport.models.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GhlApiData {
    private List<JsonNode> opportunityList;
    private List<JsonNode> eventsJson;
    private JsonNode pipelineJson;
}

