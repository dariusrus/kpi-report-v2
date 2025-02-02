package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.FollowUpConversionResponse;
import com.blc.kpiReport.schema.ghl.FollowUpConversion;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FollowUpConversionMapper {

    public FollowUpConversionResponse toResponse(FollowUpConversion followUpConversion) {
        int totalFollowUps = followUpConversion != null ? followUpConversion.getTotalFollowUps() : 0;
        int totalConversions = followUpConversion != null ? followUpConversion.getTotalConversions() : 0;
        double totalFollowUpPerConversion = followUpConversion != null ? followUpConversion.getTotalFollowUpPerConversion() : 0.0;

        return FollowUpConversionResponse.builder()
                .totalSms(followUpConversion.getTotalSms())
                .totalEmails(followUpConversion.getTotalEmails())
                .totalCalls(followUpConversion.getTotalCalls())
                .totalLiveChatMessages(followUpConversion.getTotalLiveChatMessages())
                .totalFollowUps(totalFollowUps)
                .totalConversions(totalConversions)
                .totalFollowUpPerConversion(totalFollowUpPerConversion)
                .ghlUserName(followUpConversion.getGhlUser() != null ? followUpConversion.getGhlUser().getName() : "")
                .ghlUserId(followUpConversion.getGhlUser() != null ? followUpConversion.getGhlUser().getUserId() : "")
                .build();
    }

    public List<FollowUpConversionResponse> toResponseList(List<FollowUpConversion> followUpConversions) {
        if (followUpConversions == null || followUpConversions.isEmpty()) return Collections.emptyList();
        return followUpConversions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}