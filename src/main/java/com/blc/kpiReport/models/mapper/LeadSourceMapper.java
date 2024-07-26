package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.LeadSourceResponse;
import com.blc.kpiReport.schema.LeadSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class LeadSourceMapper {

    public LeadSourceResponse toResponse(LeadSource leadSource) {
        if (leadSource == null) {
            return null;
        }
        return LeadSourceResponse.builder()
            .source(leadSource.getSource())
            .totalLeads(leadSource.getTotalLeads())
            .totalValues(leadSource.getTotalValues())
            .open(leadSource.getOpen())
            .won(leadSource.getWon())
            .lost(leadSource.getLost())
            .abandoned(leadSource.getAbandoned())
            .winPercentage(roundToTwoDecimalPlaces(leadSource.getWinPercentage()))
            .build();
    }

    public List<LeadSourceResponse> toResponseList(List<LeadSource> leadSources) {
        return leadSources.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}

