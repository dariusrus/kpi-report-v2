package com.blc.kpiReport.models.response.ghl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ConversationMessageResponse {
    private String messageType;
    private String direction;
    private String status;
    private int callDuration;
    private Instant dateAdded;
    private String messageBody;
}