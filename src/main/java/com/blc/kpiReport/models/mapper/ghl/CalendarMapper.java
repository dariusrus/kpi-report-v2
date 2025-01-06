package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.CalendarResponse;
import com.blc.kpiReport.schema.ghl.Calendar;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CalendarMapper {

    private final AppointmentMapper appointmentMapper;
    private final AppointmentOpportunityMapper appointmentOpportunityMapper;

    public CalendarMapper(AppointmentMapper appointmentMapper,
                          AppointmentOpportunityMapper appointmentOpportunityMapper) {
        this.appointmentMapper = appointmentMapper;
        this.appointmentOpportunityMapper = appointmentOpportunityMapper;
    }

    public CalendarResponse toResponse(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        return CalendarResponse.builder()
            .calendarId(calendar.getCalendarGhlId())
            .calendarName(calendar.getCalendarName())
            .appointments(appointmentMapper.toResponseList(calendar.getAppointments()))
            .appointmentOpportunities(appointmentOpportunityMapper.toResponseList(
                    calendar.getAppointmentOpportunities()
            ))
            .build();
    }

    public List<CalendarResponse> toResponseList(List<Calendar> calendars) {
        return calendars.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}