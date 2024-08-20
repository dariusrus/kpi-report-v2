package com.blc.kpiReport.models.response.ghl;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppointmentResponse {
    private String status;
    private int count;
    private double percentage;
}

