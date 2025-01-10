package com.blc.kpiReport.models.response.ghl;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ContactScheduledAppointmentResponse {
    private String contactName;
    private boolean scheduledACall;
}

