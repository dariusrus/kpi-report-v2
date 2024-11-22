package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.ContactResponse;
import com.blc.kpiReport.models.response.ghl.PipelineResponse;
import com.blc.kpiReport.models.response.ghl.PipelineStageResponse;
import com.blc.kpiReport.models.response.ghl.SalesPersonConversionResponse;
import com.blc.kpiReport.schema.ghl.GhlContact;
import com.blc.kpiReport.schema.ghl.PipelineStage;
import com.blc.kpiReport.schema.ghl.SalesPersonConversion;
import org.springframework.stereotype.Component;

import java.util.Comparator;
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
                        .salesPersonConversions(mapToSalesPersonConversionResponseList(stage.getSalesPersonConversions()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<SalesPersonConversionResponse> mapToSalesPersonConversionResponseList(List<SalesPersonConversion> salesPersonConversions) {
        return salesPersonConversions.stream()
                .map(conversion -> SalesPersonConversionResponse.builder()
                        .salesPersonId(conversion.getGhlUser() != null ? conversion.getGhlUser().getUserId() : "")
                        .salesPersonName(conversion.getGhlUser() != null ? conversion.getGhlUser().getName() : "")
                        .photoUrl(conversion.getGhlUser() != null ? conversion.getGhlUser().getPhotoUrl() : "")
                        .count(conversion.getCount())
                        .monetaryValue(conversion.getMonetaryValue())
                        .convertedContacts(mapToContactResponseList(conversion.getConvertedGhlContacts()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ContactResponse> mapToContactResponseList(List<GhlContact> contacts) {
        return contacts.stream()
                .map(contact -> ContactResponse.builder()
                        .contactId(contact.getGhlId())
                        .contactName(contact.getName())
                        .contactEmail(contact.getEmail())
                        .contactPhone(contact.getPhone())
                        .build())
                .sorted(Comparator.comparing(ContactResponse::getContactName))
                .collect(Collectors.toList());
    }
}
