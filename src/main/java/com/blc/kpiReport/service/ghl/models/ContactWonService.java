package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.schema.ghl.ContactWon;
import com.blc.kpiReport.repository.ContactWonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactWonService {

    private final ContactWonRepository repository;

    public ContactWonService(ContactWonRepository repository) {
        this.repository = repository;
    }

    public List<ContactWon> saveAll(List<ContactWon> contactsWon) {
        return repository.saveAll(contactsWon);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
