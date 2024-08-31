package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.LeadContactResponse;
import com.blc.kpiReport.models.response.ghl.LeadSourceResponse;
import com.blc.kpiReport.schema.ghl.LeadSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class LeadSourceMapper {

    private final LeadContactMapper leadContactMapper;

    public LeadSourceMapper(LeadContactMapper leadContactMapper) {
        this.leadContactMapper = leadContactMapper;
    }

    public LeadSourceResponse toResponse(LeadSource leadSource) {
        if (leadSource == null) {
            return null;
        }

        List<LeadContactResponse> leadContactResponses = leadContactMapper.toResponseList(leadSource.getLeadContacts());

        return LeadSourceResponse.builder()
            .source(leadSource.getSource())
            .totalLeads(leadSource.getTotalLeads())
            .totalValues(leadSource.getTotalValues())
            .open(leadSource.getOpen())
            .won(leadSource.getWon())
            .lost(leadSource.getLost())
            .abandoned(leadSource.getAbandoned())
            .winPercentage(roundToTwoDecimalPlaces(leadSource.getWinPercentage()))
            .leadContacts(leadContactResponses) // Map and include LeadContacts
            .leadType(leadSource.getLeadType())
            .build();
    }

    public List<LeadSourceResponse> toResponseList(List<LeadSource> leadSources) {
        return leadSources.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}