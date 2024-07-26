package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.AppointmentResponse;
import com.blc.kpiReport.schema.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
            .status(appointment.getStatus())
            .count(appointment.getCount())
            .percentage(roundToTwoDecimalPlaces(appointment.getPercentage()))
            .build();
    }

    public List<AppointmentResponse> toResponseList(List<Appointment> appointments) {
        return appointments.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
