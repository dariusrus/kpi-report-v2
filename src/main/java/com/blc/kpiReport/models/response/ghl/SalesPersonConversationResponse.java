package com.blc.kpiReport.models.response.ghl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SalesPersonConversationResponse {
    private String salesPersonId;
    private String salesPersonName;
    private String contactId;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private Instant lastManualMessageDate;
    private String lastMessageType;
    private String ownerPhotoUrl;
    private List<ConversationMessageResponse> conversationMessages;
}