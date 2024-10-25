package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.LeadContactRepository;
import com.blc.kpiReport.schema.ghl.LeadContact;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadContactService {

    private final LeadContactRepository repository;

    public LeadContactService(LeadContactRepository repository) {
        this.repository = repository;
    }

    public List<LeadContact> saveAll(List<LeadContact> leadContacts) {
        return repository.saveAll(leadContacts);
    }
}
