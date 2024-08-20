package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.schema.ghl.Appointment;
import com.blc.kpiReport.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<Appointment> saveAll(List<Appointment> appointments) {
        return repository.saveAll(appointments);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
