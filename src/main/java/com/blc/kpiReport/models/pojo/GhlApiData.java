package com.blc.kpiReport.models.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GhlApiData {
    private List<JsonNode> opportunityList;
    private List<JsonNode> createdAtOpportunityList;
    private Map<String, JsonNode> createdAtContactMap;
    private List<JsonNode> lastStageChangeOpportunityList;
    private List<JsonNode> contactsWonOpportunityList;
    private Map<String, JsonNode> contactsWonContactMap;
    private Map<JsonNode, List<JsonNode>> calendarMap;
    private Map<String, JsonNode> ownerMap;
    private JsonNode pipelineJson;
}

