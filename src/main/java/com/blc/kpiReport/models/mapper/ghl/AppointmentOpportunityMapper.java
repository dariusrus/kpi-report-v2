package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.AppointmentOpportunityResponse;
import com.blc.kpiReport.schema.ghl.AppointmentOpportunity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentOpportunityMapper {

    public AppointmentOpportunityResponse toResponse(AppointmentOpportunity appointmentOpportunity) {
        return AppointmentOpportunityResponse.builder()
                .status(appointmentOpportunity.getStatus())
                .appointmentDate(appointmentOpportunity.getAppointmentDate())
                .lastStageChangeAt(appointmentOpportunity.getLastStageChangeAt())
                .pipelineName(appointmentOpportunity.getPipelineName())
                .stageName(appointmentOpportunity.getStageName())
                .contactName(appointmentOpportunity.getGhlContact() != null
                        ? appointmentOpportunity.getGhlContact().getName()
                        : "Unknown Contact")
                .build();
    }

    public List<AppointmentOpportunityResponse> toResponseList(List<AppointmentOpportunity> appointmentOpportunities) {
        return appointmentOpportunities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}