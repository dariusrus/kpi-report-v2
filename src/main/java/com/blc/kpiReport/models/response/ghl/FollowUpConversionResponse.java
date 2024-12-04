package com.blc.kpiReport.models.response.ghl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowUpConversionResponse {
    private int totalSms;
    private int totalEmails;
    private int totalCalls;
    private int totalLiveChatMessages;
    private int totalFollowups;
    private int totalConversions;
    private double followUpPerConversion;
    private String ghlUserName;
    private String ghlUserId;
}