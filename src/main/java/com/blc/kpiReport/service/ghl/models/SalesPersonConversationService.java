package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.ContactWonRepository;
import com.blc.kpiReport.repository.ghl.SalesPersonConversationRepository;
import com.blc.kpiReport.schema.ghl.ContactWon;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesPersonConversationService {

    private final SalesPersonConversationRepository repository;

    public SalesPersonConversationService(SalesPersonConversationRepository repository) {
        this.repository = repository;
    }

    public List<SalesPersonConversation> saveAll(List<SalesPersonConversation> salesPersonConversations) {
        return repository.saveAll(salesPersonConversations);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
