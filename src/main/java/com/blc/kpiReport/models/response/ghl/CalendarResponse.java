package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CalendarResponse {
    private String calendarId;
    private String calendarName;
    private List<AppointmentResponse> appointments;
}