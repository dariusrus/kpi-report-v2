package com.blc.kpiReport.models.response.ghl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowUpConversionResponse {
//    private int sms;
//    private int emails;
//    private int calls;
//    private int liveChatMessages;
//    private int followUps;
//    private int conversions;
//    private double followUpPerConversion;
    private int totalSms;
    private int totalEmails;
    private int totalCalls;
    private int totalLiveChatMessages;
    private int totalFollowUps;
    private int totalConversions;
    private double totalFollowUpPerConversion;
    private String ghlUserName;
    private String ghlUserId;
}