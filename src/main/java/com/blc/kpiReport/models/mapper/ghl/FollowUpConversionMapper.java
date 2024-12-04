package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.FollowUpConversionResponse;
import com.blc.kpiReport.schema.ghl.FollowUpConversion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FollowUpConversionMapper {

    public FollowUpConversionResponse toResponse(FollowUpConversion followUpConversion) {
        return FollowUpConversionResponse.builder()
                .totalSms(followUpConversion.getTotalSms())
                .totalEmails(followUpConversion.getTotalEmails())
                .totalCalls(followUpConversion.getTotalCalls())
                .totalLiveChatMessages(followUpConversion.getTotalLiveChatMessages())
                .totalFollowups(followUpConversion.getTotalFollowups())
                .totalConversions(followUpConversion.getTotalConversions())
                .followUpPerConversion(followUpConversion.getFollowUpPerConversion())
                .ghlUserName(followUpConversion.getGhlUser() != null ? followUpConversion.getGhlUser().getName() : "")
                .ghlUserId(followUpConversion.getGhlUser() != null ? followUpConversion.getGhlUser().getUserId() : "")
                .build();
    }

    public List<FollowUpConversionResponse> toResponseList(List<FollowUpConversion> followUpConversions) {
        return followUpConversions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}