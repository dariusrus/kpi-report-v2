package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.ContactScheduledAppointmentRepository;
import com.blc.kpiReport.schema.ghl.ContactScheduledAppointment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactScheduledAppointmentService {

    private final ContactScheduledAppointmentRepository repository;

    public ContactScheduledAppointmentService(ContactScheduledAppointmentRepository repository) {
        this.repository = repository;
    }

    public List<ContactScheduledAppointment> saveAll(List<ContactScheduledAppointment> contactScheduledAppointments) {
        return repository.saveAll(contactScheduledAppointments);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }

    public List<ContactScheduledAppointment> findAllByGoHighLevelReportId(Long goHighLevelReportId) {
        return repository.findAllByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
