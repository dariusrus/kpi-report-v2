package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentOpportunityResponse {
    private String status;
    private Instant appointmentDate;
    private Instant lastStageChangeAt;
    private String pipelineName;
    private String stageName;
    private String contactName;
}

