package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.PipelineResponse;
import com.blc.kpiReport.models.response.PipelineStageResponse;
import com.blc.kpiReport.schema.PipelineStage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class PipelineStageMapper {

    public List<PipelineResponse> toResponseList(List<PipelineStage> pipelineStages) {
        var stagesByPipeline = pipelineStages.stream()
            .collect(Collectors.groupingBy(PipelineStage::getPipelineName));

        return stagesByPipeline.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String pipelineName = entry.getKey();
                List<PipelineStage> stages = entry.getValue();
                int totalCount = stages.stream().mapToInt(PipelineStage::getCount).sum();
                List<PipelineStageResponse> pipelineStageResponses = mapToPipelineStageResponseList(stages);
                return PipelineResponse.builder()
                    .pipelineName(pipelineName)
                    .totalCount(totalCount)
                    .pipelineStages(pipelineStageResponses)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<PipelineStageResponse> mapToPipelineStageResponseList(List<PipelineStage> pipelineStages) {
        return pipelineStages.stream()
            .map(stage -> PipelineStageResponse.builder()
                .stageName(stage.getStageName())
                .count(stage.getCount())
                .percentage(roundToTwoDecimalPlaces(stage.getPercentage()))
                .monetaryValue(stage.getMonetaryValue())
                .build())
            .collect(Collectors.toList());
    }
}
