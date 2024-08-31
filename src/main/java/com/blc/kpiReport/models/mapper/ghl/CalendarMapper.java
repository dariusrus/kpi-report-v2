package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.CalendarResponse;
import com.blc.kpiReport.schema.ghl.Calendar;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CalendarMapper {

    private final AppointmentMapper appointmentMapper;

    public CalendarMapper(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    public CalendarResponse toResponse(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        return CalendarResponse.builder()
            .calendarId(calendar.getCalendarGhlId())
            .calendarName(calendar.getCalendarName())
            .appointments(appointmentMapper.toResponseList(calendar.getAppointments()))
            .build();
    }

    public List<CalendarResponse> toResponseList(List<Calendar> calendars) {
        return calendars.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}