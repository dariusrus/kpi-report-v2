package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.AppointmentOpportunityRepository;
import com.blc.kpiReport.schema.ghl.AppointmentOpportunity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentOpportunityService {

    private final AppointmentOpportunityRepository repository;

    public AppointmentOpportunityService(AppointmentOpportunityRepository repository) {
        this.repository = repository;
    }

    public List<AppointmentOpportunity> saveAll(List<AppointmentOpportunity> appointmentOpportunities) {
        return repository.saveAll(appointmentOpportunities);
    }
}
