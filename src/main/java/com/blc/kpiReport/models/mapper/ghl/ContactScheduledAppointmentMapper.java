package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.ContactScheduledAppointmentResponse;
import com.blc.kpiReport.schema.ghl.ContactScheduledAppointment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContactScheduledAppointmentMapper {

    public ContactScheduledAppointmentResponse toResponse(ContactScheduledAppointment contactScheduledAppointment) {
        return ContactScheduledAppointmentResponse.builder()
                .contactName(contactScheduledAppointment.getContactName())
                .scheduledACall(contactScheduledAppointment.isScheduledACall())
                .build();
    }

    public List<ContactScheduledAppointmentResponse> toResponseList(List<ContactScheduledAppointment> contactScheduledAppointments) {
        return contactScheduledAppointments.stream()
            .map(this::toResponse)
            .sorted((a, b) -> {
                if (a.isScheduledACall() != b.isScheduledACall()) {
                    return Boolean.compare(b.isScheduledACall(), a.isScheduledACall());
                }
                return a.getContactName().compareToIgnoreCase(b.getContactName());
            })
            .collect(Collectors.toList());
    }
}
